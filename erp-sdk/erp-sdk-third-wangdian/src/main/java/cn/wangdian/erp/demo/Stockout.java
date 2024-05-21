package cn.wangdian.erp.demo;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockout.StockoutAPI;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateTransferStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateTransferStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.ProcessStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.ProcessStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.SalesStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.SalesStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.StockoutSearchRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.StockoutSearchResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

public class Stockout
{
	public static void main(String[] args) throws WdtErpException
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "zyOther",
				"423c25002f36c7445ccd7742ea5d1be4:8f034c5a36d5749a438951cde1963f2a");
		StockoutAPI stockoutAPI = ApiFactory.get(client, StockoutAPI.class);
//		testCreateOtherOutOrder(stockoutAPI);
		// searchProcessStockout(stockoutAPI);
//		 querySalesStockout(stockoutAPI);
		// testCreateTransferOrder(stockoutAPI);
		testStockoutSearch(stockoutAPI);
	}

	private static void querySalesStockout(StockoutAPI stockoutApi) throws WdtErpException
	{
		int pageSize = 5;

		SalesStockoutRequest request = new SalesStockoutRequest();
		// request.setStockoutNo("CK2019112057");
//		request.setWarehouseNo("1001");
		// request.setStatusType(SalesStockoutRequest.STATUS_TYPE_CONSIGNED);
		request.setStartTime("2020-10-10 00:00:00");
		request.setEndTime("2020-10-12 00:00:00");

		//获取过去某个时间段的数据, 先获取总条数, 之后倒序翻页获取数据
		Pager pager = new Pager(pageSize, 0, true);
		SalesStockoutResponse response = stockoutApi.querySales(request, pager);
		Integer totalCount = response.getTotal();
		if (totalCount == null || totalCount == 0)
		{
			System.out.println("没有符合条件的结果.");
			return;
		}
		int totalPage = (totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1) - 1;
		pager.setCalcTotal(false);
		System.out.println("total_page: " + totalPage);

		// 结果仅有一页
		if (totalPage == 0)
		{
			System.out.println("我在这里处理数据");
		}
		else
		{
			for (int i = totalPage; i >= 0; i--)// 从后向前翻页
			{
				pager.setPageNo(i);
				System.out.print("pager: page_size:" + pageSize + "  page_no: " + i + "  ");
				response = stockoutApi.querySales(request, pager);

				String out = response.getOrderList().stream().map(SalesStockoutResponse.OrderInfoDto::getTradeNo)
						.collect(Collectors.joining(","));
				System.out.println("接收到的单据, 我需要做去重: " + out);
			}
		}
	}

	private static void searchProcessStockout(StockoutAPI stockoutApi)
	{
		ProcessStockoutRequest request = new ProcessStockoutRequest();
		request.setProcessNo("PS2020062202");
		ProcessStockoutResponse response = stockoutApi.searchProcess(request, new Pager(50, 0, true));

	}

	private static void testCreateOtherOutOrder(StockoutAPI stockoutAPI)
	{
		CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
		CreateOtherStockoutRequest.GoodsList inrequest1 = new CreateOtherStockoutRequest.GoodsList();
		// CreateOtherStockoutRequest.GoodsList inrequest2 = new
		// CreateOtherStockoutRequest.GoodsList();
		request.setWarehouseNo("1001");
		request.setOuterNo("CG201911286258");
		request.setRemark("测试1");
		request.setReason("1");
		inrequest1.setSpecNo("PC_2016");
		inrequest1.setPositionNo("J-3");
		inrequest1.setNum(BigDecimal.valueOf(3));
		List<CreateOtherStockoutRequest.GoodsList> list = new ArrayList<>();
		list.add(inrequest1);
		request.setGoodsList(list);

		CreateOtherStockoutResponse response = stockoutAPI.createOtherOutOrder(request);
		response.toString();
	}

	private static void testCreateTransferOrder(StockoutAPI stockoutAPI)
	{
		CreateTransferStockoutRequest.orderInfoDto orderInfo = new CreateTransferStockoutRequest.orderInfoDto();
		CreateTransferStockoutRequest.detailDto detailDto1 = new CreateTransferStockoutRequest.detailDto();

		orderInfo.setSrcOrderNo("TF202003020004");
		orderInfo.setWarehouseNo("lz");
		orderInfo.setRemark("调拨出库单新建");
		detailDto1.setNum("1");
		detailDto1.setSpecNo("lz41");
		detailDto1.setUnitName("lz1");

		List<CreateTransferStockoutRequest.detailDto> detailList = new ArrayList<>();
		detailList.add(detailDto1);

		CreateTransferStockoutResponse response = stockoutAPI.createTransferOrder(orderInfo, detailList, true);
		response.toString();
	}

	private static void testStockoutSearch(StockoutAPI api) throws WdtErpException
	{
		int pageSize = 5;
		Pager pager = new Pager(pageSize, 0, true);
		StockoutSearchRequest request = new StockoutSearchRequest();
		//		request.setStartTime("2023-01-11 17:18:21");
		//		request.setEndTime("2023-01-11 18:18:21");
		//		request.setOrderType((byte)1);
		request.setStockoutNo("CK2023041715");

		StockoutSearchResponse response = api.search(request, pager);
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
