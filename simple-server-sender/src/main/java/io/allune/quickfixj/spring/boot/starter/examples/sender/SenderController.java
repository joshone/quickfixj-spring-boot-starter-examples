/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.allune.quickfixj.spring.boot.starter.examples.sender;

import io.allune.quickfixj.spring.boot.starter.examples.sender.customfields.NoCompDealerQuotes;
import io.allune.quickfixj.spring.boot.starter.examples.sender.customfields.NoRefPrices;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import quickfix.Acceptor;
import quickfix.DoubleField;
import quickfix.IntField;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.StringField;
import quickfix.field.AvgPx;
import quickfix.field.BidPx;
import quickfix.field.CalculatedCcyLastQty;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.Currency;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastForwardPoints;
import quickfix.field.LastMkt;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastSpotRate;
import quickfix.field.LeavesQty;
import quickfix.field.MDEntryDate;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MarketSegmentID;
import quickfix.field.OnBehalfOfCompID;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.PartyID;
import quickfix.field.PartyIDSource;
import quickfix.field.PartyRole;
import quickfix.field.PartySubID;
import quickfix.field.PartySubIDType;
import quickfix.field.Price;
import quickfix.field.Product;
import quickfix.field.QtyType;
import quickfix.field.QuoteEntryID;
import quickfix.field.QuoteID;
import quickfix.field.QuoteReqID;
import quickfix.field.SecurityType;
import quickfix.field.SendingTime;
import quickfix.field.SettlCurrAmt;
import quickfix.field.SettlCurrency;
import quickfix.field.SettlDate;
import quickfix.field.SettlType;
import quickfix.field.Side;
import quickfix.field.Spread;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TradeDate;
import quickfix.field.TradePublishIndicator;
import quickfix.field.TransactTime;
import quickfix.fix42.QuoteRequest;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.OK;
import static quickfix.FixVersions.BEGINSTRING_FIX41;
import static quickfix.FixVersions.BEGINSTRING_FIXT11;

@RestController
public class SenderController {

	private static final Map<String, Map<String, Message>> messageMap = createMessageMap();
	private final QuickFixJTemplate quickFixJTemplate;
	private final Acceptor serverAcceptor;

	public SenderController(QuickFixJTemplate serverQuickFixJTemplate, Acceptor serverAcceptor) {
		this.quickFixJTemplate = serverQuickFixJTemplate;
		this.serverAcceptor = serverAcceptor;
	}

	private static HashMap<String, Map<String, Message>> createMessageMap() {
		HashMap<String, Map<String, Message>> stringMapHashMap = new HashMap<>();
		stringMapHashMap.put(BEGINSTRING_FIX41, initialiseFix41MessageMap());
		stringMapHashMap.put(BEGINSTRING_FIXT11, initialiseFix50MessageMap());
		return stringMapHashMap;
	}

	private static Map<String, Message> initialiseFix41MessageMap() {
		Map<String, Message> messageMap = new HashMap<>();
		messageMap.put("OrderCancelRequest", new quickfix.fix41.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Symbol("LNUX"),
				new Side(Side.BUY)));
		return messageMap;
	}

	private static Map<String, Message> initialiseFix50MessageMap() {
		Map<String, Message> messageMap = new HashMap<>();
		messageMap.put("Quote", new quickfix.fix50.Quote(new QuoteID("123")));
		return messageMap;
	}

	@RequestMapping("/send-message")
	@ResponseStatus(OK)
	public void sendMessage(@RequestParam String fixVersion, @RequestParam String messageType) {

		Map<String, Message> stringMessageMap = messageMap.get(fixVersion);
		Message message = stringMessageMap.get(messageType);
		message.setField(new StringField(Text.FIELD, "Text: " + randomUUID().toString()));

		SessionID sessionID = serverAcceptor.getSessions().stream()
				.filter(id -> id.getBeginString().equals(fixVersion))
				.findFirst()
				.orElseThrow(RuntimeException::new);
		quickFixJTemplate.send(message, sessionID);
	}

	@GetMapping(path = "/path1")
	public ResponseEntity<?> getPath1() throws SessionNotFound {
		QuoteRequest quoteRequest = createQuoteRequest(UUID.randomUUID());
		SessionID sessionID = new SessionID("FIX.4.2", "EXEC", "BANZAI");
		Session.sendToTarget(quoteRequest, sessionID);

		return ResponseEntity.ok("OK");
	}

	@GetMapping(path = "/path2")
	public ResponseEntity<?> getPath2() throws SessionNotFound {
		QuoteRequest quoteRequest = createQuoteRequest(UUID.randomUUID());
		SessionID sessionID = new SessionID("FIX.4.2", "EXEC", "BANZAI");
		Session.sendToTarget(quoteRequest, sessionID);

		return ResponseEntity.ok("OK");

	}
	
	@GetMapping(path = "/execution-report-bloomberg-spot")
	public ResponseEntity<?> executionReportBloombergSpot() throws SessionNotFound{
		ExecutionReport executionReport = new ExecutionReport(new OrderID("3-2-805950014T-0-0"), new ExecID("3-2-805950014T-0-0"), new ExecType(ExecType.TRADE), 
				new OrdStatus(OrdStatus.FILLED), new Side(Side.SELL), new LeavesQty(0), new CumQty(1), new AvgPx(755.93));
		SessionID sessionID = new SessionID("FIX.4.4", "EXEC", "BANZAI");
		OnBehalfOfCompID onBehalfOfCompId = new OnBehalfOfCompID("FX");
		Header header = executionReport.getHeader();
		header.setField(144, onBehalfOfCompId);
		
		executionReport.set(new TransactTime());
		executionReport.set(new SettlType(SettlType.REGULAR_FX_SPOT_SETTLEMENT));
		executionReport.set(new SettlDate("20201116"));
		executionReport.set(new SettlCurrency("CLP"));
		executionReport.set(new SettlCurrAmt(756));
		executionReport.set(new OrderQty(1));
		executionReport.set(new Spread(0));
		executionReport.set(new OrdType(OrdType.MARKET));
		executionReport.set(new Product(Product.CURRENCY));
//		executionReport.set(new TradePublishIndicator(TradePublishIndicator.DO_NOT_PUBLISH_TRADE));
		executionReport.set(new ClOrdID("3-2-805950014T-0-0"));
		executionReport.set(new Symbol("USD/CLP"));
		executionReport.set(new Currency("USD"));
//		executionReport.setField(133, new OfferPx(756.5675));
		executionReport.setField(75, new TradeDate("20201111"));
		executionReport.setField(167, new SecurityType(SecurityType.FX_SPOT));
		
		executionReport.setField(30, new LastMkt("XOFF"));
		executionReport.setField(31, new LastPx(757.9015));
		executionReport.setField(32, new LastQty(1));
		executionReport.setField(132, new BidPx(755.93));
		executionReport.setField(194, new LastSpotRate(755.93));
		executionReport.setField(195, new LastForwardPoints(0));
		executionReport.setField(new StringField(797, "Y"));
		executionReport.setField(854, new QtyType(QtyType.UNITS));
		executionReport.setField(1056, new CalculatedCcyLastQty(756));
		executionReport.setField(new StringField(1057, "Y"));
		executionReport.setField(1300, new MarketSegmentID("XOFF"));
		executionReport.setField(1390, new TradePublishIndicator(0));
		
		//NoCompDealerQuotes
		NoCompDealerQuotes noCompDealerQuotesGroup = new NoCompDealerQuotes();
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGD1"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 755.93));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 755.93));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGD2"));
		noCompDealerQuotesGroup.setField(new IntField(10011, 0));
		noCompDealerQuotesGroup.setField(new IntField(22485, 0));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGD5"));
		noCompDealerQuotesGroup.setField(new IntField(10011, 0));
		noCompDealerQuotesGroup.setField(new IntField(22485, 0));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "MidRate"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 756.15));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 756.15));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "RefRate"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 755.93));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 755.93));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGDM Mid"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 756.15));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 756.15));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGDM Nts"));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		//fin NoCompDealerQuotes
		//NoRefPrices
		NoRefPrices noRefPricesGroup = new NoRefPrices();
		noRefPricesGroup.setField(new DoubleField(22079, 755.93));
		noRefPricesGroup.setField(new IntField(22080, 20));
		noRefPricesGroup.setField(new IntField(22081, 12));
		
		executionReport.addGroup(noRefPricesGroup);
		//fin NoRefPrices
		
		ExecutionReport.NoPartyIDs noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("SCOTT"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(1));
		
		//NoPartySubIDsGroup
		ExecutionReport.NoPartyIDs.NoPartySubIDs noPartySubIDsGroup = new ExecutionReport.NoPartyIDs.NoPartySubIDs();
		noPartySubIDsGroup.set(new PartySubID("SCOTIABANK SUD AMERICANO ( CLIENT DESK ), SANTIAGO, CHILE"));
		noPartySubIDsGroup.set(new PartySubIDType(1));//party sub id
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("16601104"));
		noPartySubIDsGroup.set(new PartySubIDType(2));//person
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("CRISTOBAL PALACIOS"));
		noPartySubIDsGroup.set(new PartySubIDType(9));//contact name
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		executionReport.addGroup(noPartyIDsGroup);
		
		//2ndo grupo
		noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("REPO"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(13));
		
		noPartySubIDsGroup = new ExecutionReport.NoPartyIDs.NoPartySubIDs();
		
		noPartySubIDsGroup.set(new PartySubID("SCOTIA CORREDORA DE BOLSA CHILE"));
		noPartySubIDsGroup.set(new PartySubIDType(1));//firm
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("27971561"));
		noPartySubIDsGroup.set(new PartySubIDType(2));//person
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("DIEGO SUSBIELLES"));
		noPartySubIDsGroup.set(new PartySubIDType(9));//contact
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		executionReport.addGroup(noPartyIDsGroup);
		
		//3er grupo
		noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("PRODUCT TYPE"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(16));
		
		noPartySubIDsGroup = new ExecutionReport.NoPartyIDs.NoPartySubIDs();
		
		noPartySubIDsGroup.set(new PartySubID("Dealing (RFQ)"));
		noPartySubIDsGroup.set(new PartySubIDType(4));//application
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		executionReport.addGroup(noPartyIDsGroup);
		
		//4to grupo 
		noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("16601104"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(11));//OrderOriginationTrader
		executionReport.addGroup(noPartyIDsGroup);
		
		Session.sendToTarget(executionReport, sessionID);
		return ResponseEntity.ok("OK");
	}
	
	@GetMapping(path = "/execution-report-bloomberg-forward")
	public ResponseEntity<?> executionReportBloombergForward() throws SessionNotFound{
		ExecutionReport executionReport = new ExecutionReport(new OrderID("3-2-805950014T-0-0"), new ExecID("3-2-816816706M-0-0"), new ExecType(ExecType.TRADE), 
				new OrdStatus(OrdStatus.FILLED), new Side(Side.SELL), new LeavesQty(0), new CumQty(1000), new AvgPx(755.93));
		SessionID sessionID = new SessionID("FIX.4.4", "EXEC", "BANZAI");
		OnBehalfOfCompID onBehalfOfCompId = new OnBehalfOfCompID("FX");
		Header header = executionReport.getHeader();
		header.setField(144, onBehalfOfCompId);
		
		executionReport.setField(new StringField(60, "20210531-18:27:09.940"));
		executionReport.set(new SettlType("M3"));
		executionReport.set(new SettlDate("20210902"));
		executionReport.set(new SettlCurrency("CLP"));
		executionReport.set(new SettlCurrAmt(725700));
		executionReport.set(new OrderQty(1000));
		executionReport.set(new Spread(0));
		executionReport.set(new OrdType(OrdType.MARKET));
		executionReport.set(new Product(Product.CURRENCY));
//		executionReport.set(new TradePublishIndicator(TradePublishIndicator.DO_NOT_PUBLISH_TRADE));
		executionReport.set(new ClOrdID("3-2-816816706M-0-0"));
		executionReport.set(new Symbol("USD/CLP"));
		executionReport.set(new Currency("USD"));
//		executionReport.setField(133, new OfferPx(756.5675));
		executionReport.setField(75, new TradeDate("20210531"));
		executionReport.setField(167, new SecurityType(SecurityType.FX_FORWARD));
		
		executionReport.setField(30, new LastMkt("XOFF"));
		executionReport.setField(31, new LastPx(725.70));
		executionReport.setField(32, new LastQty(1000));
		executionReport.setField(132, new BidPx(725.70));
		executionReport.setField(194, new LastSpotRate(725));
		executionReport.setField(195, new LastForwardPoints(0.7));
		executionReport.setField(new StringField(797, "Y"));
		executionReport.setField(854, new QtyType(QtyType.UNITS));
		executionReport.setField(1056, new CalculatedCcyLastQty(756));
		executionReport.setField(new StringField(1057, "Y"));
		executionReport.setField(1300, new MarketSegmentID("XOFF"));
		executionReport.setField(1390, new TradePublishIndicator(0));
		
		//NoCompDealerQuotes
		NoCompDealerQuotes noCompDealerQuotesGroup = new NoCompDealerQuotes();
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGD1"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 755.93));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 755.93));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGD2"));
		noCompDealerQuotesGroup.setField(new IntField(10011, 0));
		noCompDealerQuotesGroup.setField(new IntField(22485, 0));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGD5"));
		noCompDealerQuotesGroup.setField(new IntField(10011, 0));
		noCompDealerQuotesGroup.setField(new IntField(22485, 0));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "MidRate"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 756.15));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 756.15));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "RefRate"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 755.93));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 755.93));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGDM Mid"));
		noCompDealerQuotesGroup.setField(new DoubleField(10011, 756.15));
		noCompDealerQuotesGroup.setField(new DoubleField(22485, 756.15));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		
		noCompDealerQuotesGroup.setField(new StringField(10010, "BGDM Nts"));
		noCompDealerQuotesGroup.setField(new IntField(22486, 0));
		
		executionReport.addGroup(noCompDealerQuotesGroup);
		//fin NoCompDealerQuotes
		//NoRefPrices
		NoRefPrices noRefPricesGroup = new NoRefPrices();
		noRefPricesGroup.setField(new DoubleField(22079, 755.93));
		noRefPricesGroup.setField(new IntField(22080, 20));
		noRefPricesGroup.setField(new IntField(22081, 12));
		
		executionReport.addGroup(noRefPricesGroup);
		//fin NoRefPrices
		
		ExecutionReport.NoPartyIDs noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("SCOT"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(1));
		
		//NoPartySubIDsGroup
		ExecutionReport.NoPartyIDs.NoPartySubIDs noPartySubIDsGroup = new ExecutionReport.NoPartyIDs.NoPartySubIDs();
		noPartySubIDsGroup.set(new PartySubID("SCOTIABANK SUD AMERICANO ( CLIENT DESK ), SANTIAGO, CHILE"));
		noPartySubIDsGroup.set(new PartySubIDType(1));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("16601104"));
		noPartySubIDsGroup.set(new PartySubIDType(2));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("CRISTOBAL PALACIOS"));
		noPartySubIDsGroup.set(new PartySubIDType(9));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		executionReport.addGroup(noPartyIDsGroup);
		
		//2ndo grupo
		noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("REPO"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(13));
		
		noPartySubIDsGroup = new ExecutionReport.NoPartyIDs.NoPartySubIDs();
		
		noPartySubIDsGroup.set(new PartySubID("SCOTIA CORREDORA DE BOLSA CHILE"));
		noPartySubIDsGroup.set(new PartySubIDType(1));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("27971561"));
		noPartySubIDsGroup.set(new PartySubIDType(2));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		noPartySubIDsGroup.set(new PartySubID("DIEGO SUSBIELLES"));
		noPartySubIDsGroup.set(new PartySubIDType(9));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		executionReport.addGroup(noPartyIDsGroup);
		
		//3er grupo
		noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("PRODUCT TYPE"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(16));
		
		noPartySubIDsGroup = new ExecutionReport.NoPartyIDs.NoPartySubIDs();
		
		noPartySubIDsGroup.set(new PartySubID("Dealing (RFQ)"));
		noPartySubIDsGroup.set(new PartySubIDType(4));
		noPartyIDsGroup.addGroup(noPartySubIDsGroup);
		
		executionReport.addGroup(noPartyIDsGroup);
		
		//4to grupo 
		noPartyIDsGroup = new ExecutionReport.NoPartyIDs();
		noPartyIDsGroup.setField(new PartyID("16601104"));
		noPartyIDsGroup.setField(new PartyIDSource('D'));
		noPartyIDsGroup.setField(new PartyRole(11));
		executionReport.addGroup(noPartyIDsGroup);
		
		Session.sendToTarget(executionReport, sessionID);
		return ResponseEntity.ok("OK");
	}
//	AllocationInstruction

	@GetMapping(path = "/execution-report-celer")
	public ResponseEntity<?> executionReportCeler() throws SessionNotFound{
		ExecutionReport executionReport = new ExecutionReport(new OrderID("2076603489517051904"), new ExecID("2076603492218183680"), new ExecType(ExecType.FILL), 
				new OrdStatus(OrdStatus.FILLED), new Side(Side.BUY), new LeavesQty(0), new CumQty(1000000), new AvgPx(1.183054));
		SessionID sessionID = new SessionID("FIX.4.4", "BNS-LATAM-UAT-MD", "BNS-LATAM-RFQ-MD");
		
		
		
		executionReport.set(new OrderQty(1000000));
		executionReport.set(new OrdType(OrdType.LIMIT));
		executionReport.set(new ClOrdID("ordID_1"));
		executionReport.set(new Symbol("EUR/USD"));
		executionReport.set(new Currency("EUR"));
		executionReport.set(new LastPx(1.183054));
		executionReport.set(new LastQty(1000000));
		
		executionReport.set(new Price(1.18313));
		
		
		executionReport.set(new TimeInForce(TimeInForce.FILL_OR_KILL));
		executionReport.set(new TransactTime());//60
		executionReport.set(new SettlType("1W"));//63
		executionReport.set(new SettlDate("20200915"));//64
		
		executionReport.set(new LastSpotRate(1.18317));//194
		executionReport.set(new LastForwardPoints(-0.00004));//195
		
		executionReport.setField(new StringField(461, "FORWARD"));
		Session.sendToTarget(executionReport, sessionID);
		return ResponseEntity.ok("OK");
	}
	private QuoteRequest createQuoteRequest(UUID operationId) {
		return new QuoteRequest(new QuoteReqID(operationId.toString()));
	}


	@GetMapping(path = "/market-data-full-refresh")
	public ResponseEntity<?> marketDataFullRefresh() throws SessionNotFound {

		MarketDataSnapshotFullRefresh marketData = new MarketDataSnapshotFullRefresh();
		
		SessionID sessionID = new SessionID("FIX.4.4", "BNS-LATAM-UAT-MD", "BNS-LATAM-RFQ-MD");
		Header header = marketData.getHeader();
		
		marketData.setField(new SendingTime());
		marketData.setField(new StringField(55, "EUR/USD"));
		marketData.setField(new StringField(262, "FIXLOADTEST:1500959671701"));
		//marketData.setField(new MarketDepth(1));
		marketData.setField(new StringField(461, "SPOT")); //CFICode
		//marketData.setField(new NoMDEntries(2));
		
		MarketDataSnapshotFullRefresh.NoMDEntries group = new MarketDataSnapshotFullRefresh.NoMDEntries();
		group.set(new MDEntryType('0'));
		group.set(new MDEntryPx(1.1337));
		group.set(new MDEntrySize(1600000));
		group.set(new QuoteEntryID("1009707692782002208"));
		group.set(new MDEntryDate());
		
		marketData.addGroup(group);
		
		group.set(new MDEntryType('1'));
		group.set(new MDEntryPx(1.13373));
		group.set(new MDEntrySize(2000000));
		group.set(new QuoteEntryID("1009707692782002209"));
		group.set(new MDEntryDate());
		
		marketData.addGroup(group);
		
		Session.sendToTarget(marketData, sessionID);
		return ResponseEntity.ok("OK");

	}
}
