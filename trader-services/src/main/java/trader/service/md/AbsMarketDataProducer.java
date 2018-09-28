package trader.service.md;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import trader.common.exchangeable.Exchange;
import trader.common.exchangeable.Exchangeable;
import trader.common.exchangeable.ExchangeableType;
import trader.common.util.ConversionUtil;
import trader.common.util.StringUtil;

public abstract class AbsMarketDataProducer implements AutoCloseable, MarketDataProducer {
    private final static Logger logger = LoggerFactory.getLogger(AbsMarketDataProducer.class);

    protected MarketDataServiceImpl service;
    protected String id;
    protected volatile Status status;
    protected volatile long statusTime;
    protected Properties connectionProps;
    protected long tickCount;
    protected List<String> subscriptions;

    protected AbsMarketDataProducer(MarketDataServiceImpl service, Map map){
        this.service = service;
        id = ConversionUtil.toString(map.get("id"));
        status = Status.Initialized;
        connectionProps = StringUtil.text2properties((String)map.get("text"));
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", getType().name());
        json.addProperty("status", status.name());
        json.addProperty("statusTime", statusTime);
        json.addProperty("tickCount", tickCount);
        JsonArray a = new JsonArray();
        for(String s:subscriptions) {
            a.add(s);
        }
        json.add("subscriptions", a);
        return json;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Properties getConnectionProps() {
        return connectionProps;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public long getStatusTime() {
        return statusTime;
    }

    @Override
	public boolean canSubscribe(Exchangeable e) {
    	if ( e.getType()==ExchangeableType.FUTURE ) {
    	    Exchange exchange = e.exchange();
    	    if ( exchange==Exchange.SHFE || exchange==Exchange.CZCE || exchange==Exchange.DCE || exchange==Exchange.CFFEX ) {
    	        return true;
    	    }
    	}
        return false;
    }

    /**
     * 检查配置是否发生变化
     */
    public boolean configEquals(Map map) {
        Properties connectionProps2 = StringUtil.text2properties((String)map.get("text"));
        return connectionProps.equals(connectionProps2);
    }

    @Override
    public void close() {
        subscriptions.clear();
        close0();
    }

    protected abstract void close0();

    /**
     * 异步连接
     */
    public abstract void connect();

    /**
     * 订阅, 需要等到连接上之后才能调用
     */
    public abstract void subscribe(Collection<Exchangeable> exchangeables);

    protected void changeStatus(Status newStatus) {
        if ( status!=newStatus ) {
            Status lastStatus = status;
            this.status = newStatus;
            logger.info(getId()+" status changed to "+status+", last status: "+lastStatus);
            statusTime = System.currentTimeMillis();
            if ( null!=service ) {
                service.onProducerStatusChanged(this, lastStatus);
            }
        }
    }

    protected void notifyData(MarketData md) {
        tickCount++;
        service.onProducerData(md);
    }
}
