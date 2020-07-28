package trader.service.tradlet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import trader.common.beans.BeansContainer;
import trader.common.exception.AppException;
import trader.common.exchangeable.Exchangeable;
import trader.common.util.DateUtil;
import trader.common.util.JsonEnabled;
import trader.common.util.JsonUtil;
import trader.common.util.PriceUtil;
import trader.common.util.UUIDUtil;
import trader.service.ServiceErrorConstants;
import trader.service.md.MarketData;
import trader.service.repository.BOEntity;
import trader.service.repository.BORepository;
import trader.service.repository.BORepositoryConstants.BOEntityType;
import trader.service.trade.MarketTimeService;
import trader.service.trade.Order;
import trader.service.trade.OrderImpl;
import trader.service.trade.TradeConstants;
import trader.service.trade.TradeService;
import trader.service.trade.Transaction;

/**
 * 管理某个交易分组的报单和成交计划
 */
public class PlaybookKeeperImpl implements PlaybookKeeper, TradeConstants, TradletConstants, ServiceErrorConstants, JsonEnabled {
    private static final Logger logger = LoggerFactory.getLogger(PlaybookKeeperImpl.class);

    private String entityId;
    private TradletGroupImpl group;
    private MarketTimeService mtService;
    private List<Order> allOrders = new ArrayList<>();
    private LinkedList<Order> pendingOrders = new LinkedList<>();
    private LinkedHashMap<String, PlaybookImpl> allPlaybooks = new LinkedHashMap<>();
    private LinkedList<PlaybookImpl> activePlaybooks = new LinkedList<>();

    public PlaybookKeeperImpl(TradletGroupImpl group) {
        this.group = group;
        entityId = group.getId()+":PlaybookKeeper";
        BeansContainer beansContainer = group.getBeansContainer();
        mtService = beansContainer.getBean(MarketTimeService.class);
        TradeService tradeService = beansContainer.getBean(TradeService.class);
        if ( tradeService.getType()==TradeServiceType.RealTime ) {
            restorePlaybooks(beansContainer);
        }
    }

    public void update(String configText) {
    }

    @Override
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(allOrders);
    }

    @Override
    public Collection<Order> getPendingOrders() {
        return Collections.unmodifiableCollection(pendingOrders);
    }

    @Override
    public Order getLastOrder() {
        if ( allOrders.isEmpty() ) {
            return null;
        }
        return allOrders.get(allOrders.size()-1);
    }

    @Override
    public Order getLastPendingOrder() {
        if ( pendingOrders.isEmpty() ) {
            return null;
        }
        return pendingOrders.peekLast();
    }

    @Override
    public void cancelAllPendingOrders() {
        if ( !pendingOrders.isEmpty() ) {
            List<Order> orders0 = new ArrayList<>(pendingOrders);
            for(Order order:orders0) {
                if ( order.getStateTuple().getState().isRevocable() ) {
                    try {
                        group.getAccount().cancelOrder(order.getId());
                    } catch (AppException e) {
                        logger.error("组 "+group.getId()+" 取消报单 "+order.getId()+" 失败 "+e.toString(), e);
                    }
                }
            }
        }
    }

    @Override
    public Collection<Playbook> getAllPlaybooks() {
        return (Collection)Collections.unmodifiableCollection(allPlaybooks.values());
    }

    @Override
    public List<Playbook> getActivePlaybooks(Exchangeable instrument) {
        List<Playbook> result = new ArrayList<>();
        if ( null==instrument) {
            result = (List)activePlaybooks;
        }else{
            for(Playbook pb:activePlaybooks) {
                if ( instrument.equals(pb.getInstrument())) {
                    result.add(pb);
                }
            }
        }
        return result;
    }

    @Override
    public Playbook getPlaybook(String playbookId) {
        return allPlaybooks.get(playbookId);
    }

    @Override
    public Playbook createPlaybook(Tradlet tradlet, PlaybookBuilder builder) throws AppException
    {
        if ( group.getState()!=TradletGroupState.Enabled ) {
            throw new AppException(ERR_TRADLET_TRADLETGROUP_NOT_ENABLED, "Tradlet group "+group.getId()+" is not enabled");
        }
        String playbookId = BOEntity.ID_PREFIX_PLAYBOOK+UUIDUtil.genUUID58();
        if ( builder.getInstrument()==null ) {
            builder.setInstrument(group.getInstruments().get(0));
        }
        PlaybookImpl playbook = new PlaybookImpl(group, playbookId, builder);
        if ( tradlet!=null ) {
            playbook.setAttr(PBATTR_TRADLET_ID.name(), group.getTradletId(tradlet));
        }
        allPlaybooks.put(playbookId, playbook);
        activePlaybooks.add(playbook);
        if ( logger.isInfoEnabled()) {
            logger.info("组 "+group.getId()+" 交易剧本 "+playbookId+" 创建: "+builder.getAttrs());
        }
        group.onPlaybookStateChanged(playbook, null);
        asyncSaveState();
        return playbook;
    }

    @Override
    public boolean closePlaybook(Playbook playbook0, PlaybookCloseReq closeReq) {
        boolean result = false;
        if ( playbook0!=null ) {
            PlaybookImpl playbook = (PlaybookImpl)playbook0;
            PlaybookStateTuple pbStateTuple = playbook.getStateTuple();
            PlaybookState pbState = pbStateTuple.getState();
            switch(pbState) {
            case Opening: //开仓过程中, 取消报单
                result = playbook.cancelOpeningOrder();
                break;
            case Opened: //已开仓, 平仓
                result = playbook.closeOpenedOrder(closeReq.getActionId());
                break;
            default:
                result = false;
                break;
            }
            if ( result ) {
                if ( closeReq.getTimeout()>0 ) {
                    playbook.setAttr(Playbook.PBATTR_CLOSE_TIMEOUT.name(), ""+closeReq.getTimeout());
                }
                if ( logger.isInfoEnabled()) {
                    logger.info("组 "+group.getId()+" 关闭交易剧本 "+playbook.getId()+" action id "+closeReq.getActionId()+" at "+DateUtil.date2str(mtService.getMarketTime()));
                }
            }
        }
        asyncSaveState();
        return result;
    }

    public void updateOnTxn(Order order, Transaction txn) {
        PlaybookImpl playbook = null;
        if ( order!=null ) {
            String playbookId = order.getAttr(Order.ODRATTR_PLAYBOOK_ID);
            playbook = allPlaybooks.get(playbookId);
        }
        if ( playbook!=null ) {
            playbook.updateOnTxn(order, txn);
        }
        asyncSaveState();
    }

    /**
     * 更新订单状态
     */
    public void updateOnOrder(Order order) {
        String playbookId = order.getAttr(Order.ODRATTR_PLAYBOOK_ID);
        PlaybookImpl playbook = allPlaybooks.get(playbookId);
        if ( playbook==null ) {
            return;
        }
        boolean saveState = false;
        if ( order.getStateTuple().getState().isDone() ) {
            pendingOrders.remove(order);
            saveState = true;
        }
        PlaybookStateTuple oldStateTuple = playbook.updateStateOnOrder(order);
        if ( oldStateTuple!=null ) {
            saveState |= playbookChangeStateTuple(playbook, oldStateTuple,"Order "+order.getRef()+" "+order.getInstrument()+" D:"+order.getDirection()+" P:"+PriceUtil.long2str(order.getLimitPrice())+" V:"+order.getVolume(OdrVolume.ReqVolume)+" F:"+order.getOffsetFlags()+" at "+DateUtil.date2str(mtService.getMarketTime()));
        }
        if (saveState) {
            asyncSaveState();
        }
    }

    public void updateOnTick(MarketData tick) {
        boolean saveState = false;
        for(PlaybookImpl playbook:activePlaybooks) {
            PlaybookStateTuple oldStateTuple = playbook.updateStateOnTick(tick);
            if ( oldStateTuple!=null ) {
                saveState |= playbookChangeStateTuple(playbook, oldStateTuple, "noop");
            }
        }
        if ( saveState ) {
            asyncSaveState();
        }
    }

    /**
     * 判断超时Playbook
     */
    public void onNoopSecond() {
        boolean saveState = false;
        for(PlaybookImpl playbook:activePlaybooks) {
            PlaybookStateTuple oldStateTuple = playbook.updateStateOnNoop();
            if ( oldStateTuple!=null ) {
                saveState |= playbookChangeStateTuple(playbook, oldStateTuple, "noop");
            }
        }
        if ( saveState ) {
            asyncSaveState();
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("accountId", group.getAccount().getId());
        json.add("allOrderIds", JsonUtil.identifierIds2json((Collection)allOrders));
        json.add("pendingOrderIds", JsonUtil.identifierIds2json((Collection)pendingOrders));
        json.add("activePlaybookIds", JsonUtil.identifierIds2json((Collection)pendingOrders));
        json.add("allPlaybookIds", JsonUtil.object2json(allPlaybooks.keySet()));
        return json;
    }

    private boolean playbookChangeStateTuple(PlaybookImpl playbook, PlaybookStateTuple oldStateTuple, String time) {
        boolean toSave = false;
        if ( oldStateTuple!=null ) {
            PlaybookState newState = playbook.getStateTuple().getState();
            int lastOrderCount = playbook.getOrders().size();
            logger.info("组 "+group.getId()+" 交易剧本 "+playbook.getId()+" 状态改变, 从 "+oldStateTuple.getState()+" 到 "+newState+" 于 "+time);
            List<Order> playbookOrders = playbook.getOrders();
            //检查是否有新的报单
            if ( lastOrderCount!=playbookOrders.size() ) {
                Order newOrder = playbookOrders.get(lastOrderCount);
                addOrder(newOrder);
                toSave = true;
            }
            //检查Playbook状态
            if ( newState.isDone() ) {
                activePlaybooks.remove(playbook);
                toSave = true;
            }
            group.onPlaybookStateChanged(playbook, oldStateTuple);
        }
        return toSave;
    }

    private void addOrder(Order order) {
        allOrders.add(order);
        pendingOrders.add(order);
    }

    /**
     * 从数据库加载本交易日的Playbook
     */
    private void restorePlaybooks(BeansContainer beansContainer) {
        BORepository repository = group.getRepository();
        LocalDate tradingDay = group.getMarketTimeService().getTradingDay();
        String text = repository.load(BOEntityType.Default, entityId);
        if ( !StringUtil.isEmpty(text) ) {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            //还原当日Order
            List<String> discardOrderIds = new ArrayList<>();
            List<String> restoredOrderIds = new ArrayList<>();
            JsonArray allOrderIds = json.get("allOrderIds").getAsJsonArray();
            for(int i=0;i<allOrderIds.size();i++) {
                String orderId = allOrderIds.get(i).getAsString();
                OrderImpl order = OrderImpl.load(repository, orderId, null);
                if ( order.getTradingDay().equals(tradingDay) ) {
                    this.allOrders.add(order);
                    if ( !order.getStateTuple().getState().isDone() ) {
                        this.pendingOrders.add(order);
                    }
                    restoredOrderIds.add(orderId);
                } else {
                    discardOrderIds.add(orderId);
                }
            }
            List<String> discardPbIds = new ArrayList<>();
            List<String> restoredPbIds = new ArrayList<>();
            JsonArray playbookIds = json.get("allPlaybookIds").getAsJsonArray();
            for(int i=0;i<playbookIds.size();i++) {
                String pbId = playbookIds.get(i).getAsString();
                PlaybookImpl pb = PlaybookImpl.load(repository, pbId, null);
                if ( pb.getStateTuple().getState().isDone() && !pb.getStateTuple().getTradingDay().equals(tradingDay)) {
                    discardPbIds.add(pbId);
                } else {
                    pb.setGroup(group);
                    restoredPbIds.add(pbId);
                    this.allPlaybooks.put(pbId, pb);
                    if ( !pb.getStateTuple().getState().isDone() ) {
                        this.activePlaybooks.add(pb);
                    }
                }
            }
            logger.info("组 "+group.getId()+" 恢复交易剧本: "+restoredPbIds+", 报单: "+restoredOrderIds+", 丢弃结束历史交易剧本: "+discardPbIds+", 历史报单: "+discardOrderIds);
        }
    }

    private void asyncSaveState() {
        group.getRepository().asynSave(BOEntityType.Default, entityId, this);
    }

}
