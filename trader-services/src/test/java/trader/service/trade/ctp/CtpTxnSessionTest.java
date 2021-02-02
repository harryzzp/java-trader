package trader.service.trade.ctp;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import trader.service.trade.TradeConstants.OrderState;
import trader.service.trade.TradeConstants.OrderSubmitState;

public class CtpTxnSessionTest {

    @Test
    public void testContract() {
        Pattern contractPattern = Pattern.compile("\\w+\\d+");
        assertTrue( contractPattern.matcher("l1908").matches() );
        assertTrue( !contractPattern.matcher("m1812-C-3150").matches() );
    }
/*
2019-01-10 08:59:04,426 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          16,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10650.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=16,OrderLocalID=        5151,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:04,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=44953,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 08:59:04,439 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          16,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10650.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=16,OrderLocalID=        5151,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=4,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=5,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:04,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=已撤单报单被拒绝CZCE:出错: 现在不是交易时间,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=44953,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 08:59:26,981 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          23,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10649.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=23,OrderLocalID=        5160,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:26,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=44973,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 08:59:26,999 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          23,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10649.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=23,OrderLocalID=        5160,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=4,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=5,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:26,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=已撤单报单被拒绝CZCE:出错: 现在不是交易时间,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=44973,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 08:59:46,668 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          32,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10655.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030150,RequestID=32,OrderLocalID=        5055,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030150,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:46,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=45008,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 08:59:46,686 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          32,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10655.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030150,RequestID=32,OrderLocalID=        5055,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030150,InstallID=1,OrderSubmitStatus=4,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=5,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:46,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=已撤单报单被拒绝CZCE:出错: 现在不是交易时间,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=45008,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 09:00:25,764 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10646.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=38,OrderLocalID=        5364,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=09:00:25,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=46383,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 09:00:25,780 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10646.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=38,OrderLocalID=        5364,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=2019011001225417,OrderSource=0,OrderStatus=3,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=09:00:25,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=12836,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=未成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=46383,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 09:00:26,331 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10646.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=38,OrderLocalID=        5364,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=2019011001225417,OrderSource=0,OrderStatus=3,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=09:00:25,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=12842,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=未成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=46383,RelativeOrderSysID=,ZCETotalTradedVolume=1,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 09:00:26,333 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10646.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=38,OrderLocalID=        5364,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=2019011001225417,OrderSource=0,OrderStatus=0,OrderType=0,VolumeTraded=1,VolumeTotal=0,InsertDate=20190110,InsertTime=09:00:25,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=12842,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=全部成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=46383,RelativeOrderSysID=,ZCETotalTradedVolume=1,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
2019-01-10 09:00:26,336 [etp696165690-16] ERROR t.s.t.a.hyqh-zhugf.AccountImpl - Account hyqh-zhugf order is not found           38 with txn id: 2019011000902281
2019-01-10 09:00:26,338 [etp696165690-16] ERROR t.service.trade.TradeServiceImpl - Async event process failed on data CThostFtdcTradeField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,ExchangeID=CZCE,TradeID=2019011000902281,Direction=1,OrderSysID=2019011001225417,ParticipantID=0203,ClientID=35721699,TradingRole=1,ExchangeInstID=AP905,OffsetFlag=0,HedgeFlag=1,Price=10646.0,Volume=1,TradeDate=20190110,TradeTime=09:00:26,TradeType=0,PriceSource=0,TraderID=02030140,OrderLocalID=        5364,ClearingPartID=0203,BusinessUnit=02030140,SequenceNo=12843,TradingDay=20190110,SettlementID=1,BrokerOrderSeq=46383,TradeSource=0,InvestUnitID=]
2019-01-10 09:14:31,280 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          14,UserID=901203125,OrderPriceType=2,Direction=0,CombOffsetFlag=1,CombHedgeFlag=1,LimitPrice=10480.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=14,OrderLocalID=        6335,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=09:14:31,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1671160344,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=54549,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=61.148.199.174,MacAddress=EC8914A7E2FA]
2019-01-10 09:14:31,300 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - IGNORE order return from other CTP session: CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          14,UserID=901203125,OrderPriceType=2,Direction=0,CombOffsetFlag=1,CombHedgeFlag=1,LimitPrice=10480.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=14,OrderLocalID=        6335,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=2019011001564458,OrderSource=0,OrderStatus=3,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=09:14:31,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=15700,FrontID=5,SessionID=-1671160344,UserProductInfo=PBMFPV1000,StatusMsg=未成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=54549,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=61.148.199.174,MacAddress=EC8914A7E2FA]
2019-01-10 09:54:24,738 [Thread-14] INFO  t.s.t.a.hyqh-zhugf.CtpTxnSession - OnRtnFromBankToFutureByBank: CThostFtdcRspTransferField[TradeCode=102001,BankID=10,BankBranchID=0000,BrokerID=1080,BrokerBranchID=0000,TradeDate=20190110,TradeTime=09:54:24,BankSerial=YQ0009499201,TradingDay=20190110,PlateSerial=35,LastFragment=0,SessionID=-1513226213,CustomerName=朱国峰,IdCardType=1,IdentifiedCardNo=321022197712037211,CustType=0,BankAccount=6226090103835399,BankPassWord=******,AccountID=901203125,Password=******,InstallID=1,FutureSerial=815,UserID=,VerifyCertNoFlag=0,CurrencyID=CNY,TradeAmount=20000.0,FutureFetchAmount=0.0,FeePayFlag= ,CustFee=0.0,BrokerFee=0.0,Message=,Digest=,BankAccType=1,DeviceID=,BankSecuAccType= ,BrokerIDByBank=00780000,BankSecuAcc=,BankPwdFlag= ,SecuPwdFlag=0,OperNo=,RequestID=0,TID=98311,TransferStatus=0,ErrorID=0,ErrorMsg=交易成功,LongCustomerName=朱国峰]
 */


    /**
     * 测试报单失败状态
     */
    @Test
    public void testOrderState_Sequence() {
        //Placed
        //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          16,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10650.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=16,OrderLocalID=        5151,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:04,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=44953,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
        //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          16,UserID=901203125,OrderPriceType=2,Direction=0,CombOffsetFlag=1,CombHedgeFlag=1,LimitPrice=10678.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=16,OrderLocalID=        9289,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=0,NotifySequence=0,TradingDay=20190111,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=a,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190111,InsertTime=11:04:36,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=129714421,UserProductInfo=PBMFPV1000,StatusMsg=报单已提交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=81129,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.235,MacAddress=EC8914A7E2FA]
        {
            OrderSubmitState s = CtpUtil.ctp2orderSubmitState('0');
            assertTrue(s==OrderSubmitState.InsertSubmitted);
            OrderState os = CtpUtil.ctp2orderState('a', '0');
            assertTrue(os==OrderState.Submitted);
        }

        //Rejected
        //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          16,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10650.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=16,OrderLocalID=        5151,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=4,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=,OrderSource=0,OrderStatus=5,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=08:59:04,ActiveTime=,SuspendTime=,UpdateTime=,CancelTime=,ActiveTraderID=,ClearingPartID=,SequenceNo=0,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=已撤单报单被拒绝CZCE:出错: 现在不是交易时间,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=44953,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
        {
            OrderSubmitState s = CtpUtil.ctp2orderSubmitState('4');
            assertTrue(s==OrderSubmitState.InsertRejected);
            OrderState os = CtpUtil.ctp2orderState('5', '4');
            assertTrue(os==OrderState.Failed);
        }
        //Accepted
        //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10646.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=38,OrderLocalID=        5364,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=2019011001225417,OrderSource=0,OrderStatus=3,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190110,InsertTime=09:00:25,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=12836,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=未成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=46383,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
       {
           OrderSubmitState s = CtpUtil.ctp2orderSubmitState('3');
           assertTrue(s==OrderSubmitState.Accepted);
           OrderState os = CtpUtil.ctp2orderState('3', '3');
           assertTrue(os==OrderState.Accepted);
       }
       //Complete
       //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          38,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10646.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=38,OrderLocalID=        5364,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190110,SettlementID=1,OrderSysID=2019011001225417,OrderSource=0,OrderStatus=0,OrderType=0,VolumeTraded=1,VolumeTotal=0,InsertDate=20190110,InsertTime=09:00:25,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=12842,FrontID=5,SessionID=-1732503983,UserProductInfo=PBMFPV1000,StatusMsg=全部成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=46383,RelativeOrderSysID=,ZCETotalTradedVolume=1,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.223,MacAddress=EC8914A7E2FA]
       {
           OrderSubmitState s = CtpUtil.ctp2orderSubmitState('3');
           assertTrue(s==OrderSubmitState.Accepted);
           OrderState os = CtpUtil.ctp2orderState('0', '3');
           assertTrue(os==OrderState.Complete);
       }
       //Canceled
       //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          16,UserID=901203125,OrderPriceType=2,Direction=0,CombOffsetFlag=1,CombHedgeFlag=1,LimitPrice=10678.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=16,OrderLocalID=        9289,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190111,SettlementID=1,OrderSysID=2019011102290250,OrderSource=0,OrderStatus=5,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190111,InsertTime=11:04:36,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=23935,FrontID=5,SessionID=129714421,UserProductInfo=PBMFPV1000,StatusMsg=已撤单,UserForceClose=false,ActiveUserID=901203125,BrokerOrderSeq=81129,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=117.136.0.235,MacAddress=EC8914A7E2FA]
       //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          18,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10686.0,VolumeTotalOriginal=1,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030140,RequestID=18,OrderLocalID=        7083,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030140,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190107,SettlementID=1,OrderSysID=2019010702195837,OrderSource=0,OrderStatus=5,OrderType=0,VolumeTraded=0,VolumeTotal=1,InsertDate=20190107,InsertTime=09:23:09,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030140,ClearingPartID=,SequenceNo=26872,FrontID=5,SessionID=-1444797375,UserProductInfo=PBMFPV1000,StatusMsg=已撤单,UserForceClose=false,ActiveUserID=901203125,BrokerOrderSeq=72461,RelativeOrderSysID=,ZCETotalTradedVolume=0,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=61.148.199.174,MacAddress=EC8914A7E2FA]
       {
           OrderSubmitState s = CtpUtil.ctp2orderSubmitState('3');
           assertTrue(s==OrderSubmitState.Accepted);
           OrderState os = CtpUtil.ctp2orderState('5', '3');
           assertTrue(os==OrderState.Canceled);
       }
       //ParticallyComplete
       //CThostFtdcOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=160414,UserID=901203125,OrderPriceType=2,Direction=1,CombOffsetFlag=0,CombHedgeFlag=1,LimitPrice=10612.0,VolumeTotalOriginal=4,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=1,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=02030150,RequestID=0,OrderLocalID=       12433,ExchangeID=CZCE,ParticipantID=0203,ClientID=35721699,ExchangeInstID=AP905,TraderID=02030150,InstallID=1,OrderSubmitStatus=3,NotifySequence=1,TradingDay=20190111,SettlementID=1,OrderSysID=2019011103016278,OrderSource=0,OrderStatus=1,OrderType=0,VolumeTraded=3,VolumeTotal=1,InsertDate=20190111,InsertTime=14:44:26,ActiveTime=00:00:00,SuspendTime=00:00:00,UpdateTime=00:00:00,CancelTime=,ActiveTraderID=02030150,ClearingPartID=,SequenceNo=33431,FrontID=3,SessionID=989696291,UserProductInfo=webstock9,StatusMsg=部分成交,UserForceClose=false,ActiveUserID=,BrokerOrderSeq=112868,RelativeOrderSysID=,ZCETotalTradedVolume=4,IsSwapOrder=false,BranchID=,InvestUnitID=,AccountID=,CurrencyID=,IPAddress=61.148.199.174,MacAddress=EC8914A7E2FA]
       {
           OrderSubmitState s = CtpUtil.ctp2orderSubmitState('3');
           assertTrue(s==OrderSubmitState.Accepted);
           OrderState os = CtpUtil.ctp2orderState('1', '3');
           assertTrue(os==OrderState.ParticallyComplete);
       }
    }

    public void testErrRtnOrderInsert() {
        //CThostFtdcInputOrderField[BrokerID=1080,InvestorID=901203125,InstrumentID=AP905,OrderRef=          15,UserID=901203125,OrderPriceType=2,Direction=0,CombOffsetFlag=1,CombHedgeFlag=1,LimitPrice=10867.0,VolumeTotalOriginal=5,TimeCondition=3,GTDDate=,VolumeCondition=1,MinVolume=0,ContingentCondition=1,StopPrice=0.0,ForceCloseReason=0,IsAutoSuspend=false,BusinessUnit=,RequestID=15,UserForceClose=false,IsSwapOrder=false,ExchangeID=CZCE,InvestUnitID=,AccountID=,CurrencyID=,ClientID=35721699,IPAddress=61.148.199.174,MacAddress=EC8914A7E2FA] CThostFtdcRspInfoField[ErrorID=30,ErrorMsg=CTP:平仓量超过持仓量]
    }

    @Test
    public void testSettlementDay() {
        String line ="              制表时间 Creation Date：20210112";
        Pattern pattern = Pattern.compile("Date：(\\d{8})");
        Matcher m = pattern.matcher(line);
        assertTrue(m.find());
        assertTrue(m.group(1).equals("20210112"));
    }
}
