package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.sales.RawTradeAPI;
import cn.wangdian.erp.sdk.api.sales.dto.PushSelfRequest;
import cn.wangdian.erp.sdk.api.sales.dto.PushSelfResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MultiTenantModeTest
{
	public static void main(String[] args) throws IOException, WdtErpException
	{

		List rawTradeList = new ArrayList<PushSelfRequest.RawTrade>();
		List tradeOrderList = new ArrayList<PushSelfRequest.RawTradeOrder>();
		List tradeDiscountList = new ArrayList<PushSelfRequest.RawTradeDiscount>();
		buildRequest(rawTradeList, tradeOrderList, tradeDiscountList);
		String shopNo = "POS";

		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa", true);
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		RawTradeAPI rawTradeAPI = ApiFactory.get(client, RawTradeAPI.class);
		PushSelfResponse response = rawTradeAPI.pushSelf(shopNo, rawTradeList, tradeOrderList, tradeDiscountList);
		System.out.println("push rawTrade response: " + new GsonBuilder().create().toJson(response));
	}

	public static String getRandomString(int length)
	{
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; length > i; i++)
		{
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	private static void buildRequest(List<PushSelfRequest.RawTrade> rawTradeList, List<PushSelfRequest.RawTradeOrder> tradeOderList, List<PushSelfRequest.RawTradeDiscount> tradeDiscountList)
	{

		PushSelfRequest.RawTrade rawTrade = new PushSelfRequest.RawTrade();
		rawTrade.setAutoWms(false);
		// rawTrade.setWarehouseNo("pos_inner"); // 非自流转订单不需要设置此字段.

		rawTrade.setPostAmount(BigDecimal.valueOf(2.0000D));

		rawTrade.setOtherAmount(BigDecimal.valueOf(5.2000D));

		rawTrade.setDiscount(BigDecimal.valueOf(1.0000D));
		// rawTrade.receivable='';
		rawTrade.setPlatformCost(BigDecimal.valueOf(0.0000D));
		rawTrade.setCodAmount(BigDecimal.valueOf(0.0000D));
		rawTrade.setReceived(BigDecimal.valueOf(0.0000D));

		rawTrade.setTid(getRandomString(10));
		rawTrade.setOrderCount(20);

		rawTrade.setProcessStatus(PushSelfRequest.RawTrade.PROCESS_STATUS_WAIT_DELIVERY);
		rawTrade.setTradeStatus(PushSelfRequest.RawTrade.TRADE_STATUS_WAIT_CONSIGN);
		rawTrade.setRefundStatus(PushSelfRequest.RawTrade.REFUND_NONE);
		rawTrade.setPayStatus(PushSelfRequest.RawTrade.PAY_STATUS_FULL_PAID);

		rawTrade.setPayMethod(PushSelfRequest.RawTrade.PAY_METHOD_ONLINE); // 1在线转帐
																			// 2现金，3银行转账，4邮局汇款
																			// 5预付款
																			// 6刷卡
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateTimeStr = simpleDateFormat.format(new Date());
		// System.out.println("\n\ndateTimeStr"+dateTimeStr);
		rawTrade.setTradeTime("2020-11-20 10:00:00");
		rawTrade.setPayTime(dateTimeStr);
		rawTrade.setEndTime(dateTimeStr);

		rawTrade.setBuyerNick("test_openapi");
		rawTrade.setBuyerMessage("test_openapi");
		rawTrade.setBuyerEmail("test_openapi@gmail.com");
		rawTrade.setBuyerArea("test_area");

		rawTrade.setReceiverName("api_receiver_name");
		rawTrade.setReceiverArea("api_receiver_area");
		rawTrade.setReceiverAddress("A街道B小区C栋");
		rawTrade.setReceiverZip("000001");
		rawTrade.setReceiverMobile("123456789");
		rawTrade.setReceiverTelno("");

		rawTrade.setInvoiceType(PushSelfRequest.RawTrade.INVOICE_TYPE_NONE);
		rawTrade.setInvoiceTitle("");
		rawTrade.setInvoiceContent("");

		// rawTrade.logistics_type=-1;
		rawTrade.setConsignInterval(0);
		rawTrade.setDeliveryTerm(PushSelfRequest.RawTrade.DELIVERY_TERM_DAP);
		rawTrade.setToDeliverTime("");
		rawTrade.setPayId("");
		rawTrade.setPayAccount("");
		rawTrade.setRemark("");
		rawTrade.setRemarkFlag(0);

		for (int j = 0; j < rawTrade.getOrderCount(); j++)
		{
			PushSelfRequest.RawTradeOrder tradeOrder = new PushSelfRequest.RawTradeOrder();

			tradeOrder.setTid(rawTrade.getTid());
			tradeOrder.setOid(tradeOrder.getTid() + "_" + j);
			tradeOrder.setOrderType(PushSelfRequest.RawTradeOrder.ORDER_TYPE_NORMAL);

			tradeOrder.setStatus(PushSelfRequest.RawTradeOrder.PLATFORM_STATUS);
			tradeOrder.setRefundStatus(PushSelfRequest.RawTradeOrder.REFUND_STATUS_NONE);

			tradeOrder.setSpecId("openapi_sid_" + j);
			tradeOrder.setGoodsId("openapi_gid_" + j);
			tradeOrder.setGoodsNo("api_goods_no_" + j);
			tradeOrder.setSpecNo("api_spec_no_" + j);
			tradeOrder.setGoodsName("api_goods_name_" + j);

			tradeOrder.setNum(BigDecimal.valueOf(3.0000D));
			tradeOrder.setPrice(BigDecimal.valueOf(2.5000D));
			tradeOrder.setAdjustAmount(BigDecimal.valueOf(2.0000D));
			tradeOrder.setRefundAmount(BigDecimal.valueOf(0.0000D));
			tradeOrder.setDiscount(BigDecimal.valueOf(0.5000D)); // 优惠金额
			tradeOrder.setShareDiscount(BigDecimal.valueOf(0.20000D));// 分摊优惠金额
			// $tradeOrder->total_amount - $tradeOrder->share_discount ;
			tradeOrder.setTotalAmount(tradeOrder.getShareDiscount());

			tradeOrder.setCid("");
			tradeOrder.setRemark("");
			tradeOrder.setJson("");


			// 处理主单部分数据
			rawTrade.setGoodsCount(rawTrade.getGoodsCount().add(tradeOrder.getNum()));
			// rawTrade.receivable += tradeOrder.getShareAmount();
			tradeOderList.add(tradeOrder);
		}

		for (int j = 0; j < tradeOderList.size(); j++)
		{
			PushSelfRequest.RawTradeDiscount tradeDiscount = new PushSelfRequest.RawTradeDiscount();

			tradeDiscount.setTid(rawTrade.getTid());
			tradeDiscount.setOid(tradeOderList.get(j).getOid());
			tradeDiscount.setSn("api_sn_"+j);
			tradeDiscount.setType("api_type_"+j);
			tradeDiscount.setName("api_discount_"+j);
			tradeDiscount.setIsBonus( Byte.valueOf((byte) 0));
			tradeDiscount.setDetail("raw_discount");
			tradeDiscount.setAmount(BigDecimal.valueOf(3.20000D));

			tradeDiscountList.add(tradeDiscount);
		}

		rawTradeList.add(rawTrade);
	}
}
