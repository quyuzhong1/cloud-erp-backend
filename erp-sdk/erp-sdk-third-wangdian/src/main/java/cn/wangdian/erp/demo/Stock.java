package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.StockAPI;
import cn.wangdian.erp.sdk.api.wms.dto.PdOrderCreateRequest;
import cn.wangdian.erp.sdk.api.wms.dto.PdOrderCreateRequest.DetailDto;
import cn.wangdian.erp.sdk.api.wms.dto.StockSearch2Request;
import cn.wangdian.erp.sdk.api.wms.dto.StockSearch2Response;
import cn.wangdian.erp.sdk.api.wms.dto.StockSearch2Response.Detail;
import cn.wangdian.erp.sdk.api.wms.dto.StockSearchRequest;
import cn.wangdian.erp.sdk.api.wms.dto.StockSearchResponse;
import cn.wangdian.erp.sdk.api.wms.dto.TransferOrderCreateRequest;
import cn.wangdian.erp.sdk.api.wms.dto.TransferOrderCreateResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Stock
{
	public static void main(String[] args) throws IOException, WdtErpException
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://172.172.1.62:30000/", "xyx_api",
				"e8866c1681dacc9488a19c89991791b8:8f5800dab84b7a8d614e92cba739032c");
		StockAPI stockAPI = ApiFactory.get(client, StockAPI.class);

		// testStockSpecSearch(stockAPI);
		// testCreatePdOrder(stockAPI);
//		testTransferOrderCreate(stockAPI);
		testStockSpecSearch2(stockAPI);
	}

	private static void testStockSpecSearch(StockAPI stockApi)
	{
		StockSearchRequest request = new StockSearchRequest();

		request.setSpecNos(Arrays.asList("daba3", "daba4"));
		request.setWarehouseNo("pos");
		// request.setStartTime("2019-09-01");
		// request.setEndTime("2019-09-20");

		StockSearchResponse response = stockApi.search(request, new Pager(1, 0, true));

		if (response.getTotal() <= 0)
		{
			System.out.println("No eligible results! ");
			return;
		}

		List<StockSearchResponse.StockSearchDto> dtos = response.getStockSearchDtos();
		System.out.println("total: " + response.getTotal() + " first spec_no:" + dtos.get(0).getSpecNo());
	}

	private static void testCreatePdOrder(StockAPI stockApi)
	{
		PdOrderCreateRequest.OrderDto orderDto = new PdOrderCreateRequest.OrderDto();
		orderDto.setDefectMode(PdOrderCreateRequest.OrderDto.DEFECT_MODE_NORMAL);
		orderDto.setWarehouseNo("1001");
		orderDto.setRemark("API TEST");

		PdOrderCreateRequest.DetailDto detailDto = new PdOrderCreateRequest.DetailDto();
		detailDto.setSpecNo("PC_2018");
		detailDto.setDefect(false);
		detailDto.setNewNum("5");
		detailDto.setRemark("detail remark");

		List<DetailDto> detailList = new ArrayList<DetailDto>();
		detailList.add(detailDto);

		Map<String, Object> response = stockApi.createPdOrder(orderDto, detailList);

		// success
		if (response != null)
			System.out.println(response);
	}

	private static void testTransferOrderCreate(StockAPI stockApi)
	{
		TransferOrderCreateRequest.orderInfoDto orderInfo = new TransferOrderCreateRequest.orderInfoDto();
		TransferOrderCreateRequest.detailDto detailDto1 = new TransferOrderCreateRequest.detailDto();
		TransferOrderCreateRequest.detailDto detailDto2 = new TransferOrderCreateRequest.detailDto();

		orderInfo.setFromWarehouseNo("lz");
		orderInfo.setToWarehouseNo("jziyy");
		orderInfo.setMode(3);
		orderInfo.setRemark("调拨单新建");
		detailDto1.setNum("1");
		detailDto1.setSpecNo("lz11");
		detailDto2.setNum("3");
		detailDto2.setSpecNo("lz13");
		List<TransferOrderCreateRequest.detailDto> detailList = new ArrayList<>();
		detailList.add(detailDto1);
		detailList.add(detailDto2);
		TransferOrderCreateResponse response = stockApi.createTransferOrder(orderInfo, detailList, true);
		response.toString();

	}

	private static void testStockSpecSearch2(StockAPI stockAPI)
	{
		StockSearch2Request request = new StockSearch2Request();

		request.setStartTime("2022-05-01 03:00:00");
		request.setEndTime("2022-05-09 04:00:00");

		StockSearch2Response response = stockAPI.search2(request, new Pager(1, 0, true));

		if (response.getTotal() <= 0)
		{
			System.out.println("No eligible results! ");
			return;
		}

		List<Detail> detailList = response.getDetailList();
		System.out.println("total: " + response.getTotal() + " first spec_no:" + detailList.get(0).getSpecNo());
	}
}
