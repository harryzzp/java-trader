package trader.service.tradlet.impl.cta;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import trader.common.beans.BeansContainer;
import trader.common.beans.Discoverable;
import trader.common.exchangeable.Exchangeable;
import trader.common.exchangeable.MarketTimeStage;
import trader.common.util.ConversionUtil;
import trader.common.util.DateUtil;
import trader.common.util.FileUtil;
import trader.common.util.FileWatchListener;
import trader.common.util.JsonEnabled;
import trader.common.util.JsonUtil;
import trader.common.util.PriceUtil;
import trader.common.util.StringUtil;
import trader.common.util.TraderHomeUtil;
import trader.service.md.MarketData;
import trader.service.repository.BORepository;
import trader.service.repository.BORepositoryConstants.BOEntityType;
import trader.service.ta.LeveledBarSeries;
import trader.service.ta.TechnicalAnalysisAccess;
import trader.service.ta.TechnicalAnalysisService;
import trader.service.trade.MarketTimeService;
import trader.service.trade.Order;
import trader.service.trade.TradeConstants.OrderPriceType;
import trader.service.trade.TradeConstants.PosDirection;
import trader.service.trade.TradeConstants.TradeServiceType;
import trader.service.trade.TradeService;
import trader.service.trade.Transaction;
import trader.service.tradlet.Playbook;
import trader.service.tradlet.PlaybookBuilder;
import trader.service.tradlet.PlaybookCloseReq;
import trader.service.tradlet.PlaybookKeeper;
import trader.service.tradlet.PlaybookStateTuple;
import trader.service.tradlet.Tradlet;
import trader.service.tradlet.TradletConstants.PBMoney;
import trader.service.tradlet.TradletConstants.PlaybookState;
import trader.service.tradlet.TradletContext;
import trader.service.tradlet.TradletGroup;
import trader.service.tradlet.impl.cta.CTAConstants.CTARuleState;
/**
 * CTA辅助策略交易小程序，由 $TRADER_HOME/etc/cta-hints.xml 配置文件定义的策略驱动执行，基于xml/json语法定义
 */
@Discoverable(interfaceClass = Tradlet.class, purpose = "CTA")
public class CTATradlet implements Tradlet, FileWatchListener, JsonEnabled {
    private final static Logger logger = LoggerFactory.getLogger(CTATradlet.class);

    private final static String ATTR_CTA_RULE_ID = "ctaRuleId";

    private TradeService tradeService;
    private BeansContainer beansContainer;
    private ExecutorService executorService;
    private BORepository repository;
    private File hintConfigFile;
    private File hintStateFile;
    private MarketTimeService mtService;
    private TradletGroup group;
    private TechnicalAnalysisService taService;
    private PlaybookKeeper playbookKeeper;
    /**
     * 全部(含历史)CTAHint
     */
    private List<CTAHint> hints = new ArrayList<>();
    /**
     * 全部(含历史)CTA规则记录
     */
    private Map<String, CTARuleLog> ruleLogs = new LinkedHashMap<>();
    /**
     * 当前可进场的CTA规则记录
     */
    private Map<Exchangeable, List<CTARule>> toEnterRulesByInstrument = new LinkedHashMap<>();
    /**
     * 当前活动CTA规则记录: RuleState=toEnter,Holding
     */
    private Map<String, CTARule> activeRulesById = new LinkedHashMap<>();

    private long totalTicksRecv;

    @Override
    public void init(TradletContext context) throws Exception
    {
        beansContainer = context.getBeansContainer();
        group = context.getGroup();
        repository = beansContainer.getBean(BORepository.class);
        playbookKeeper = group.getPlaybookKeeper();
        mtService = beansContainer.getBean(MarketTimeService.class);
        taService = beansContainer.getBean(TechnicalAnalysisService.class);
        executorService = beansContainer.getBean(ExecutorService.class);
        //实际环境下, 监控hints文件
        initHintFile(context);
        loadHintLogs();
        reloadHints(context);
    }

    @Override
    public void reload(TradletContext context) throws Exception
    {

    }

    @Override
    public void destroy() {
    }

    @Override
    public Object onRequest(String path, Map<String, String> params, String payload) {
        if (StringUtil.equalsIgnoreCase("cta/hints", path) ) {
            return JsonUtil.object2json(hints);
        } else if (StringUtil.equalsIgnoreCase("cta/ruleLogs", path) ) {
            return JsonUtil.object2json(ruleLogs.values());
        } else if (path.startsWith("cta/ruleLog/") ) {
            String ruleLogId = path.substring("cta/ruleLog/".length()).trim();
            List<CTARuleLog> result= new ArrayList<>();
            for(CTARuleLog ruleLog:ruleLogs.values()) {
                if ( ruleLog.id.indexOf(ruleLogId)>=0) {
                    result.add(ruleLog);
                }
            }
            return JsonUtil.object2json(result);
        } else if (StringUtil.equalsIgnoreCase("cta/activeRules", path) ) {
            return JsonUtil.object2json(activeRulesById.values());
        } else if (StringUtil.equalsIgnoreCase("cta/activeRuleIds", path) ) {
            return JsonUtil.object2json(activeRulesById.keySet());
        } else if (StringUtil.equalsIgnoreCase("cta/toEnterInstruments", path) ) {
            return JsonUtil.object2json(toEnterRulesByInstrument.keySet());
        } else if (StringUtil.equalsIgnoreCase("cta/toEnterRules", path) ) {
            TreeSet<CTARule> toEnterRules = new TreeSet<>();
            for(CTARuleLog ruleLog:ruleLogs.values()) {
                if (ruleLog.state==CTARuleState.ToEnter ) {
                    CTARule rule = this.activeRulesById.get(ruleLog.id);
                    if (null!=rule && !rule.disabled) {
                        toEnterRules.add(rule);
                    }
                }
            }
            return JsonUtil.object2json(toEnterRules);
        } else if (StringUtil.equalsIgnoreCase("cta/loadRuleLogs", path) ) {
            loadHintLogs();
            return JsonUtil.object2json(ruleLogs.values());
        }
        return null;
    }

    @Override
    public void onPlaybookStateChanged(Playbook playbook, PlaybookStateTuple oldStateTuple)
    {
        String ruleId = ConversionUtil.toString(playbook.getAttr(ATTR_CTA_RULE_ID));
        CTARuleLog ruleLog = ruleLogs.get(ruleId);
        boolean asyncSave = false;
        if ( null!=ruleLog ) {
            LocalDateTime time = DateUtil.long2datetime(playbook.getStateTuple().getTimestamp());
            CTARuleState state0 = ruleLog.state;
            CTARule rule = activeRulesById.get(ruleLog.id);
            switch(ruleLog.state) {
            case Opening:
                //CTA规则开仓中
                switch(playbook.getStateTuple().getState()) {
                case Failed:
                case Canceled:
                case Canceling:
                    ruleLog.changeState(CTARuleState.Discarded, time+" 报单失败/未成交撤");
                    asyncSave = true;
                    break;
                case Opened:
                    ruleLog.changeState(CTARuleState.Holding, time+" 持仓中");
                    asyncSave = true;
                    break;
                }
                break;
            case Holding:
                //CTA策略已持仓, 忽然PB状态意外关闭
                switch(playbook.getStateTuple().getState()) {
                case Failed:
                case Canceled:
                case Closed:
                    if ( null!=rule ) {
                        long distToStop = Math.abs( rule.stop - playbook.getMoney(PBMoney.Close) );
                        long distToTake = Math.abs( rule.take - playbook.getMoney(PBMoney.Close) );
                        if ( distToTake<distToStop ) {
                            state0 = CTARuleState.TakeProfit;
                        } else {
                            state0 = CTARuleState.StopLoss;
                        }
                    } else {
                        state0 = CTARuleState.StopLoss;
                    }
                    ruleLog.changeState(state0, time+"\t外部平仓@"+PriceUtil.long2price(playbook.getMoney(PBMoney.Close)));
                    asyncSave = true;
                    break;
                }
                break;
            case StopLoss:
            case TakeProfit:
            case Timeout:
                //CTA策略主动立场, 看看PB状态是否正确?
                //TODO
                break;
            }
            if (ruleLog.state!=state0) {
                logger.info("CTA 规则 "+ruleLog.id+" 新状态 "+ruleLog.state+", 当交易剧本 "+playbook.getId()+" 新状态 "+playbook.getStateTuple().getState());
            } else {
                logger.info("CTA 规则 "+ruleLog.id+" 状态 "+ruleLog.state+", 交易剧本 "+playbook.getId()+" 新状态 "+playbook.getStateTuple().getState());
            }
        }
        if ( asyncSave ) {
            asyncSaveHintLogs();
        }
    }

    public void onTransaction(Order order, Transaction txn) {

    }

    @Override
    public void onTick(MarketData tick) {
        boolean changed = tryClosePlaybooks(tick);
        //changed |= ruleMatchForDiscard(tick);
        changed |= ruleMatchForOpen(tick);
        totalTicksRecv++;
        if ( changed ) {
            asyncSaveHintLogs();
        }
    }

    @Override
    public void onNewBar(LeveledBarSeries series) {
    }

    @Override
    public void onNoopSecond() {
    }

    /**
     * 匹配CTA规则
     */
    private boolean ruleMatchForDiscard(MarketData tick) {
        boolean result = false;
        //CTA只关注开市, 暂时不关注竞价
        if ( tick.mktStage!=MarketTimeStage.MarketOpen ) {
            return false;
        }
        List<CTARule> rules = toEnterRulesByInstrument.get(tick.instrument);
        if ( null!=rules ) {
            for(int i=0;i<rules.size();i++) {
                CTARule rule0 = rules.get(i);
                CTARuleLog ruleLog = ruleLogs.get(rule0.id);
                if ( ruleLog!=null && ruleLog.state==CTARuleState.ToEnter ) {
                    if ( rule0.matchDiscard(tick)) {
                        ruleLog.changeState(CTARuleState.Discarded, tick.updateTime+" 废弃@"+PriceUtil.long2price(tick.lastPrice));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 匹配CTA规则
     */
    private boolean ruleMatchForOpen(MarketData tick) {
        //CTA只关注开市, 暂时不关注竞价
        if ( tick.mktStage!=MarketTimeStage.MarketOpen ) {
            return false;
        }
        List<CTARule> toEnterRules = toEnterRulesByInstrument.get(tick.instrument);
        if ( null==toEnterRules ) {
            return false;
        }
        boolean result = false;
        TechnicalAnalysisAccess taAccess = taService.forInstrument(tick.instrument);
        for(int i=0;i<toEnterRules.size();i++) {
            CTARule rule0 = toEnterRules.get(i);
            if ( rule0.disabled || rule0.hint.finished ) { //只能平仓, 不能开仓
                continue;
            }
            CTARuleLog ruleLog = ruleLogs.get(rule0.id);
            if ( ruleLog!=null && ruleLog.state==CTARuleState.ToEnter ) {
                if ( rule0.matchEnterStrict(tick, taAccess) ) {
                    createPlaybookFromRule(rule0, tick);
                    toEnterRules.remove(rule0);
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 创建一个Playbook, 应用CTA 策略
     */
    private void createPlaybookFromRule(CTARule rule, MarketData tick) {
        PlaybookBuilder builder = new PlaybookBuilder();
        long price = tick.lastPrice;
        if ( rule.dir==PosDirection.Long ) {
            price = tick.lastAskPrice();
        } else {
            price = tick.lastBidPrice();
        }
        builder.setInstrument(tick.instrument)
            .setOpenDirection(rule.dir)
            .setVolume(rule.volume)
            .setAttr(ATTR_CTA_RULE_ID, rule.id)
            .setPriceType(OrderPriceType.LimitPrice)
            .setOpenPrice(price)
            ;
        try{
            Playbook playbook = playbookKeeper.createPlaybook(this, builder);
            CTARuleLog ruleLog = ruleLogs.get(rule.id);
            if ( ruleLog!=null ) {
                ruleLog.changeState(CTARuleState.Opening, tick.updateTime+" "+(rule.dir==PosDirection.Long?"多":"空")+"开@"+PriceUtil.long2str(price)+"*"+rule.volume);
            }
            playbook.open();
            ruleLog.pbId = playbook.getId();
            logger.info("Tradlet group "+group.getId()+" 合约 "+tick.instrument+" CTA 策略 "+rule.id+" 开仓: "+playbook.getId());
        }catch(Throwable t) {
            logger.error("Tradlet group "+group.getId()+" 合约 "+tick.instrument+" CTA 策略 "+rule.id+" 创建交易剧本失败: ", t);
        }
    }

    /**
     * 尝试关闭Playbook
     */
    private boolean tryClosePlaybooks(MarketData tick) {
        boolean result = false;
        List<Playbook> playbooks = playbookKeeper.getActivePlaybooks(null);
        if ( null!=playbooks && !playbooks.isEmpty() ) {
            for(Playbook pb:playbooks) {
                //必须是 Opened 状态, 且合约相同
                if ( !pb.getInstrument().equals(tick.instrument) || pb.getStateTuple().getState()!=PlaybookState.Opened ) {
                    continue;
                }
                String ctaRuleId = (String)pb.getAttr(ATTR_CTA_RULE_ID);
                if ( StringUtil.isEmpty(ctaRuleId) ) {
                    continue;
                }
                CTARule rule = activeRulesById.get(ctaRuleId);
                PlaybookCloseReq closeReq = null;
                if ( null!=rule ) {
                    closeReq = tryRuleMatchStop(rule, tick);
                    if ( null==closeReq ) {
                        closeReq = tryRuleMatchTake(rule,tick);
                    }
                    if ( null==closeReq ) {
                        closeReq = tryRuleMatchEnd(rule, tick);
                    }
                }
                if ( null!=closeReq ) {
                    activeRulesById.remove(ctaRuleId);
                    playbookKeeper.closePlaybook(pb, closeReq);
                    logger.info("Tradlet group "+group.getId()+" 合约 "+tick.instrument+" CTA 策略 "+rule.id+" 平仓: "+pb.getId());
                    result = true;
                }
            }
        }
        return result;
    }

    private PlaybookCloseReq tryRuleMatchStop(CTARule rule, MarketData tick) {
        PlaybookCloseReq closeReq = null;
        CTARuleLog ruleLog = ruleLogs.get(rule.id);
        if ( rule.matchStop(tick) ) {
            closeReq = new PlaybookCloseReq();
            String actionId = "stopLoss@"+PriceUtil.long2str(tick.lastPrice);
            closeReq.setActionId(actionId);
            if ( ruleLog!=null ) {
                ruleLog.changeState(CTARuleState.StopLoss, tick.updateTime+" 止损@"+PriceUtil.long2str(tick.lastPrice));
            }
            //止损时, 需要把该CTAHint下全部Rule状态置为Disabled
            for(CTARule rule0:rule.hint.rules) {
                if ( rule0.id.equals(rule.id)) {
                    continue;
                }
                CTARuleLog ruleLog0 = ruleLogs.get(rule0.id);
                if ( ruleLog0.state==CTARuleState.ToEnter ) {
                    ruleLog0.changeState(CTARuleState.Discarded, tick.updateTime+" Hint止损@"+PriceUtil.long2str(tick.lastPrice));
                }
            }
        }
        return closeReq;
    }

    private PlaybookCloseReq tryRuleMatchTake(CTARule rule, MarketData tick) {
        PlaybookCloseReq closeReq = null;
        CTARuleLog ruleLog = ruleLogs.get(rule.id);
        if ( rule.matchTake(tick)) {
            closeReq = new PlaybookCloseReq();
            closeReq.setActionId("takeProfit@"+PriceUtil.long2str(tick.lastPrice));
            if ( ruleLog!=null ) {
                ruleLog.changeState(CTARuleState.TakeProfit, tick.updateTime+" 止盈@"+PriceUtil.long2str(tick.lastPrice));
            }
        }
        return closeReq;
    }

    private PlaybookCloseReq tryRuleMatchEnd(CTARule rule, MarketData tick) {
        PlaybookCloseReq closeReq = null;
        CTARuleLog ruleLog = ruleLogs.get(rule.id);
        if ( rule.matchEnd(tick)) {
            closeReq = new PlaybookCloseReq();
            closeReq.setActionId("timeout@"+PriceUtil.long2str(tick.lastPrice));
            if ( ruleLog!=null ) {
                ruleLog.changeState(CTARuleState.Timeout, tick.updateTime+" 超时@"+PriceUtil.long2str(tick.lastPrice));
            }
        }
        return closeReq;
    }

    private void initHintFile(TradletContext context) throws IOException
    {
        Properties props = context.getConfigAsProps();
        String fileName = props.getProperty("file");
        if ( StringUtil.isEmpty(fileName)) {
            hintConfigFile = new File(TraderHomeUtil.getDirectory(TraderHomeUtil.DIR_ETC), "cta-hints.xml");
        } else {
            hintConfigFile = new File(TraderHomeUtil.getDirectory(TraderHomeUtil.DIR_ETC), fileName);
        }
        hintConfigFile = hintConfigFile.getAbsoluteFile();
        hintStateFile = FileUtil.changeSuffix(hintConfigFile, "json");
        logger.info("Group "+group.getId()+" 使用 CTA 策略文件: "+hintConfigFile);
        tradeService = beansContainer.getBean(TradeService.class);
        if ( tradeService.getType()==TradeServiceType.RealTime ) {
            FileUtil.watchOn(hintConfigFile, this);
        }
    }

    /**
     * 加载cta-hints.xml文件.
     * <BR>如文件更新, 会重复调用此函数
     * @param context 初始化的上下文
     */
    private void reloadHints(TradletContext context) throws Exception
    {
        LocalDate tradingDay = mtService.getTradingDay();
        //加载全部Hint
        List<CTAHint> allHints = CTAHint.loadHints(hintConfigFile, tradingDay);
        Map<String, CTARuleLog> ruleLogs = new LinkedHashMap<>(this.ruleLogs);
        boolean ruleLogsUpdated = false;
        Set<String> newRuleIds = new TreeSet<>();
        Set<Exchangeable> newRuleInstruments = new TreeSet<>();
        Map<Exchangeable, List<CTARule>> toEnterRulesByInstrument = new LinkedHashMap<>();
        List<String> toEnterRuleIds = new ArrayList<>();
        Map<String, CTARule> activeRulesById = new LinkedHashMap<>();
        Set<Exchangeable> activeRuleInstruments = new TreeSet<>();
        Set<String> finishedRuleIds = new TreeSet<>();
        List<CTAHint> hints = new ArrayList<>();
        for(CTAHint hint:allHints) {
            if ( hint.finished ) {
                for(CTARule rule:hint.rules) {
                    finishedRuleIds.add(rule.id);
                }
                continue;
            }
            hints.add(hint);
            //忽略不可用Hint
            boolean hintValid = hint.isValid(tradingDay);
            for(CTARule rule:hint.rules) {
                boolean ruleValid = hintValid && !rule.disabled;
                CTARuleLog ruleLog = ruleLogs.get(rule.id);
                if ( null==ruleLog && ruleValid) {
                    ruleLog = new CTARuleLog(rule);
                    ruleLogs.put(ruleLog.id, ruleLog);
                    ruleLogsUpdated = true;
                    newRuleIds.add(rule.id);
                    newRuleInstruments.add(hint.instrument);
                }
                if ( null==ruleLog ) {
                    continue;
                }
                if ( ruleValid && CTARuleState.ToEnter==ruleLog.state ) {
                    //禁用策略不允许开仓
                    List<CTARule> toEnterRules = toEnterRulesByInstrument.get(hint.instrument);
                    if ( null==toEnterRules ) {
                        toEnterRules = new ArrayList<>();
                        toEnterRulesByInstrument.put(hint.instrument, toEnterRules);
                    }
                    toEnterRules.add(rule);
                    toEnterRuleIds.add(rule.id);
                }
                boolean ruleActive = false;
                switch(ruleLog.state) {
                case ToEnter:
                    ruleActive = ruleValid;
                    break;
                case Opening:
                case Holding:
                    ruleActive = true;
                    break;
                default:
                    break;
                }
                if ( ruleActive ) {
                    //禁用策略也可以平仓
                    activeRulesById.put(rule.id, rule);
                    activeRuleInstruments.add(hint.instrument);
                    if ( null!=context ) {
                        context.addInstrument(hint.instrument);
                    }
                }
            }
        }
        //覆盖原始的值
        this.hints = hints;
        this.ruleLogs = ruleLogs;
        this.toEnterRulesByInstrument = toEnterRulesByInstrument;
        this.activeRulesById = activeRulesById;

        logger.info("Group "+group.getId()+" 加载CTA策略 "+hintConfigFile
                +", 待入场合约: "+toEnterRulesByInstrument.keySet()
                +", 待入场规则ID: "+toEnterRuleIds
                +", 活跃合约: "+activeRuleInstruments
                +", 活跃规则ID: "+activeRulesById);

        for(String ruleId:finishedRuleIds) {
            CTARuleLog ruleLog = ruleLogs.get(ruleId);
            if ( null!=ruleLog && !ruleLog.state.isDone() ) {
                ruleLog.changeState(CTARuleState.Discarded, LocalDateTime.now()+" 策略废弃");
                ruleLogsUpdated = true;
            }
        }
        if (ruleLogsUpdated) {
            asyncSaveHintLogs();
        }
    }

    @Override
    public void onFileChanged(File file) {
        try{
            reloadHints(null);
        }catch(Throwable t) {
            logger.error("CTA 策略文件 "+file+" 重新加载失败", t);
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("accountId", group.getAccount().getId());
        json.addProperty("totalTicksRecv", totalTicksRecv);
        json.add("activeRules", JsonUtil.object2json(activeRulesById.values()));
        json.addProperty("ruleLogCount", ruleLogs.size());
        return json;
    }

    /**
     * 从文件恢复状态
     */
    private void loadHintLogs() {
        String hintStates = null;
        if ( hintStateFile.exists() ) {
            try{
                hintStates = FileUtil.read(hintStateFile);
            }catch(Throwable t) {
                logger.error("Group "+group.getId()+" 读取 CTA 规则记录 "+hintStateFile+" 失败: "+t.toString(), t);
            }
        }
        if ( !StringUtil.isEmpty(hintStates) ) {
            JsonArray array = JsonParser.parseString(hintStates).getAsJsonArray();
            LinkedHashMap<String, CTARuleLog> ruleLogs = new LinkedHashMap<>();
            for(int i=0; i<array.size();i++) {
                CTARuleLog ruleLog = new CTARuleLog(array.get(i).getAsJsonObject());
                ruleLogs.put(ruleLog.id, ruleLog);
            }
            this.ruleLogs = ruleLogs;
            logger.info("Group "+group.getId()+" 加载 CTA 规则记录: "+ruleLogs.size());
        }
    }

    /**
     * 异步保存状态
     */
    private void asyncSaveHintLogs() {
        executorService.execute(()->{
            try {
                String json = JsonUtil.json2str(JsonUtil.object2json(ruleLogs.values()), true);
                FileUtil.save(hintStateFile, json);
            }catch(Throwable t) {
                logger.error("Group "+group.getId()+" 保存 CTA 规则记录 "+hintStateFile+" 失败: "+t.toString(), t);
            }
        });
    }

}
