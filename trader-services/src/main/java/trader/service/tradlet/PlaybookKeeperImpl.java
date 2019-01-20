package trader.service.tradlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import trader.common.exception.AppException;
import trader.common.util.JsonEnabled;
import trader.common.util.JsonUtil;
import trader.common.util.StringUtil;
import trader.common.util.UUIDUtil;
import trader.service.trade.Order;
import trader.service.trade.OrderBuilder;
import trader.service.trade.TradeConstants.OrderAction;
import trader.service.trade.TradeConstants.OrderDirection;
import trader.service.trade.TradeConstants.OrderOffsetFlag;
import trader.service.trade.TradeConstants.OrderPriceType;
import trader.service.trade.TradeConstants.PosDirection;
import trader.service.trade.Transaction;
import trader.service.tradlet.TradletConstants.PlaybookState;

/**
 * 管理某个交易分组的报单和成交计划
 */
public class PlaybookKeeperImpl implements PlaybookKeeper, JsonEnabled {
    private static final Logger logger = LoggerFactory.getLogger(PlaybookKeeperImpl.class);

    private TradletGroupImpl group;
    private List<Order> allOrders = new ArrayList<>();
    private LinkedList<Order> pendingOrders = new LinkedList<>();
    private LinkedHashMap<String, PlaybookImpl> allPlaybooks = new LinkedHashMap<>();
    private List<PlaybookImpl> activePlaybooks = new LinkedList<>();

    public PlaybookKeeperImpl(TradletGroupImpl group) {
        this.group = group;
    }

    @Override
    public List<Order> getAllOrders() {
        return allOrders;
    }

    @Override
    public List<Order> getPendingOrders() {
        return pendingOrders;
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
        return pendingOrders.getLast();
    }

    @Override
    public void cancelAllPendingOrders() {
        for(Order order:pendingOrders) {
            if ( order.getStateTuple().getState().isRevocable() ) {
                try {
                    group.getAccount().cancelOrder(order.getRef());
                } catch (AppException e) {
                    logger.error("Tradlet group "+group.getId()+" cancel order "+order.getRef()+" failed "+e.toString(), e);
                }
            }
        }
    }

    @Override
    public Collection<Playbook> getAllPlaybooks() {
        return (Collection)allPlaybooks.values();
    }

    @Override
    public Collection<Playbook> getActivePlaybooks() {
        return (Collection)activePlaybooks;
    }

    @Override
    public Playbook getPlaybook(String playbookId) {
        return allPlaybooks.get(playbookId);
    }

    @Override
    public void createPlaybook(PlaybookBuilder builder) throws AppException {
        String playbookId = "pbk_"+UUIDUtil.genUUID58();
        OrderBuilder odrBuilder = new OrderBuilder(group.getAccount());
        odrBuilder.setExchagneable(group.getExchangeable())
            .setDirection(builder.getOpenDirection()==PosDirection.Long?OrderDirection.Buy:OrderDirection.Sell)
            .setLimitPrice(builder.getOpenPrice())
            .setPriceType(OrderPriceType.LimitPrice)
            .setVolume(builder.getVolume())
            .setOffsetFlag(OrderOffsetFlag.OPEN)
            .setAttr(Playbook.ATTR_PLAYBOOK_ID, playbookId);
        //加载PlaybookTemplate 参数
        if ( !StringUtil.isEmpty(builder.getTemplateId())) {
            TradletService tradletService = group.getTradletService();
            Properties templateProps = tradletService.getPlaybookTemplates().get(builder.getTemplateId());
            if ( templateProps!=null ) {
                builder.mergeTemplateAttrs(templateProps);
            }
        }
        //创建报单
        Order order = group.getAccount().createOrder(odrBuilder);

        PlaybookImpl playbook = new PlaybookImpl(playbookId, builder, new PlaybookStateTupleImpl(PlaybookState.Opening, order, OrderAction.Send));
        allOrders.add(order);
        pendingOrders.add(order);
        allPlaybooks.put(playbookId, playbook);
        activePlaybooks.add(playbook);
        if ( logger.isInfoEnabled()) {
            logger.info("Tradlet group create playbook "+playbookId+" with openning order "+order.getRef());
        }
    }

    public void updateOnTxn(Transaction txn) {
        Order order = txn.getOrder();
        PlaybookImpl playbook = null;
        if ( order!=null ) {
            String playbookId = order.getAttr(Playbook.ATTR_PLAYBOOK_ID);
            playbook = allPlaybooks.get(playbookId);
        }
        if ( playbook!=null ) {
            playbook.updateOnTxn(txn);
        }
    }

    /**
     * 更新订单状态
     */
    public void updateOnOrder(Order order) {
        String playbookId = order.getAttr(Playbook.ATTR_PLAYBOOK_ID);
        PlaybookImpl playbook = allPlaybooks.get(playbookId);
        if ( playbook==null ) {
            return;
        }
        if ( order.getStateTuple().getState().isDone() ) {
            pendingOrders.remove(order);
        }
        PlaybookState newState = playbook.checkStateOnOrder(order);
        if ( newState!=null ) {
            PlaybookStateTuple newStateTuple = playbook.changeStateTuple(group.getBeansContainer(), group.getAccount(), newState);
            logger.info("Tradlet group "+group.getId()+" playbook "+playbook.getId()+" state is changed to "+newState+" on order "+order.getRef());
            if ( newStateTuple.getState().isDone() )
                activePlaybooks.remove(playbook);
        }
    }

    /**
     * TODO 判断超时Playbook
     */
    public void onNoopSecond() {
        for(PlaybookImpl playbook:activePlaybooks) {
            PlaybookState newState = playbook.checkStateOnNoop();
            if ( newState!=null ) {
                PlaybookStateTuple newStateTuple = playbook.changeStateTuple(group.getBeansContainer(), group.getAccount(), newState);
                logger.info("Tradlet group "+group.getId()+" playbook "+playbook.getId()+" state is changed to "+newState+" on noop");
                if ( newStateTuple.getState().isDone() )
                    activePlaybooks.remove(playbook);
            }
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("allOrderCount", allOrders.size());
        json.addProperty("pendingOrderCount", pendingOrders.size());
        json.addProperty("allPlaybookCount", allPlaybooks.size());
        json.add("activePlaybooks", JsonUtil.object2json(activePlaybooks));
        return json;
    }

}
