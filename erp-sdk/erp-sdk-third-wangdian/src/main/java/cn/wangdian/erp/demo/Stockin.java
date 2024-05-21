package cn.wangdian.erp.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockin.StockinAPI;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateTransferStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateTransferStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.OtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.OtherStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.ProcessStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.ProcessStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.RefundStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.RefundStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.StockinSearchRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.StockinSearchResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

public class Stockin
{
	public static void main(String[] args) throws Exception
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "zyOther",
				"423c25002f36c7445ccd7742ea5d1be4:8f034c5a36d5749a438951cde1963f2a");
		StockinAPI stockinAPI = ApiFactory.get(client, StockinAPI.class);

//		testCreateOtherOrder(stockinAPI);
		// testCreateTransferOrder(stockinAPI);
		// testOtherStockinQuery(stockinAPI);
		// ProcessStockin(stockinAPI);
		// refundStockin(stockinAPI);
		testStockinSearch(stockinAPI);
		 refundStockin(stockinAPI);
	}

	private static void refundStockin(StockinAPI stockinAPI)
	{

		RefundStockinRequest request = new RefundStockinRequest();
		request.setStartTime("2019-09-01");
		request.setEndTime("2019-10-01");

		RefundStockinResponse response = stockinAPI.searchRefund(request, new Pager(1, 0, true));
		if (response.getTotal() <= 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		for (RefundStockinResponse.OrderInfoDto order : response.getOrders())
		{
			System.out.println(order.getRefundNo() + " detail count:" + order.getDetailList().size() + " first detail:"
					+ order.getDetailList().get(0).getSpecNo());
		}

	}

	private static void ProcessStockin(StockinAPI stockinAPI)
	{

		ProcessStockinRequest request = new ProcessStockinRequest();
		request.setStartTime("2020-06-15 10:05:36");
		request.setEndTime("2020-06-17 11:05:36");
		request.setStockinNo("RK2006160023");
		ProcessStockinResponse response = stockinAPI.searchProcess(request, new Pager(50, 0, true));
		if (response.getTotal() <= 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		for (ProcessStockinResponse.OrderInfoDto order : response.getOrders())
		{
			System.out.println(order.getStockinNo() + " detail count:" + order.getDetailList().size() + " first detail:"
					+ order.getDetailList().get(0).getGoodsName());
		}

	}

	private static void testCreateOtherOrder(StockinAPI stockinAPI) throws WdtErpException
	{
		CreateOtherStockinRequest request = new CreateOtherStockinRequest();
		CreateOtherStockinRequest.GoodsList inrequest1 = new CreateOtherStockinRequest.GoodsList();
		CreateOtherStockinRequest.GoodsList inrequest2 = new CreateOtherStockinRequest.GoodsList();
		request.setOuterNo("CG201916830052");
		request.setWarehouseNo("lzx");
		request.setRemark("测试1");
		request.setReason("1");
		inrequest1.setSpecNo("whxg2");
		inrequest1.setPositionNo("J-6");
		inrequest1.setNum(BigDecimal.valueOf(20));
		Map<String, Object> map2 = new HashMap<String, Object>();
		inrequest2.setSpecNo("bgg");
		inrequest2.setPositionNo("J-7");
		inrequest2.setNum(BigDecimal.valueOf(60));
		List<CreateOtherStockinRequest.GoodsList> list = new ArrayList<>();
		list.add(inrequest1);
		list.add(inrequest2);
		request.setGoodsList(list);

		CreateOtherStockinResponse response = stockinAPI.createOtherOrder(request);
		response.toString();
	}

	private static void testCreateTransferOrder(StockinAPI stockinAPI)
	{
		CreateTransferStockinRequest.orderInfoDto orderInfo = new CreateTransferStockinRequest.orderInfoDto();
		CreateTransferStockinRequest.detailDto detailDto1 = new CreateTransferStockinRequest.detailDto();

		orderInfo.setSrcOrderNo("TF202003020004");
		orderInfo.setWarehouseNo("jziyy");
		orderInfo.setRemark("调拨入库单新建");
		detailDto1.setNum("1");
		detailDto1.setSpecNo("lz41");
		detailDto1.setUnitName("lz1");
		detailDto1.setPositionNo("b01");

		List<CreateTransferStockinRequest.detailDto> detailList = new ArrayList<>();
		detailList.add(detailDto1);

		CreateTransferStockinResponse response = stockinAPI.createTransferOrder(orderInfo, detailList, true);
		response.toString();
	}

	private static void testOtherStockinQuery(StockinAPI stockinAPI)
	{
		OtherStockinRequest request = new OtherStockinRequest();

		request.setStartTime("2020-06-2 00:00:00");
		request.setEndTime("2020-06-11 00:00:00");
		request.setStatus("80");

		OtherStockinResponse response = stockinAPI.queryWithDetail(request, new Pager(20, 0, true));
		if (response == null)
		{
			System.out.println("No eligible results!");
			return;
		}
	}

	private static void testStockinSearch(StockinAPI api) throws Exception
	{
		int pageSize = 5;
		Pager pager = new Pager(pageSize, 0, true);
		StockinSearchRequest request = new StockinSearchRequest();
		//		request.setStartTime("2023-01-11 17:18:21");
		//		request.setEndTime("2023-01-11 18:18:21");
		//		request.setOrderType((byte)1);
		request.setStockinNo("RK202304170022");

		StockinSearchResponse response = api.search(request, pager);
		Integer total = response.getTotal();
		if (null == total || pageSize >= total)
		{
			System.out.println("处理数据");
			return;
		}

		int totalPage = (total % pageSize == 0 ? total / pageSize : total / pageSize + 1) - 1;
		pager.setCalcTotal(false); //后续翻页不需要计算总条数, 可以大大减少请求时间
		for (int i = totalPage; i >= 0; i--)// 从后向前翻页
		{
			pager.setPageNo(i);
			System.out.print("pager: page_size:" + pageSize + "  page_no: " + i + "  ");
			response = api.search(request, pager);
			System.out.println(response.getOrderList());
		}
	}
}
