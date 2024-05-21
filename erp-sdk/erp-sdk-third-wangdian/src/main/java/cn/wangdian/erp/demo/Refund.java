package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.aftersales.RefundAPI;
import cn.wangdian.erp.sdk.api.aftersales.dto.RefundSearchRequest;
import cn.wangdian.erp.sdk.api.aftersales.dto.RefundSearchResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Refund
{
	public static void main(String[] args) throws IOException, WdtErpException
	{
		Client client = DefaultClient.get("wdtapi3", "test", "test");
		RefundAPI refundApi = ApiFactory.get(client, RefundAPI.class);

		RefundSearchRequest request = new RefundSearchRequest();
		request.setShopNos("POS");
		request.setModifiedFrom("2019-09-12 11:00");
		request.setModifiedTo("2019-11-24 11:26:31");

		RefundSearchResponse response = refundApi.search(request, new Pager(10, 0, true));

		if (response == null || response.getTotal() <= 0)
		{
			System.out.println("total_count: " + response.getTotal());
			return;
		}
		for (RefundSearchResponse.RefundOrderDto order : response.getOrders())
		{
			System.out.println("refund_no: " + order.getRefundNo());
			if (order.getType().equals(RefundSearchResponse.RefundOrderDto.REFUND_TYPE_SWAP_GOODS))
			{
				List<String> swapMerchantNos = order.getSwapOutDetailList().parallelStream()
						.map(RefundSearchResponse.SwapOrderInfoDto::getMerchantNo).collect(Collectors.toList());
				System.out.print(" swap order trade_no: " + order.getSwapOutTradeNo() + " merhant_nos:"
						+ StringUtils.join(swapMerchantNos, ","));
			}
			List<String> refundOrderDetailSpecNos = order.getDetailList().parallelStream()
					.map(RefundSearchResponse.RefundOrderInfoDto::getSpecNo).collect(Collectors.toList());
			System.out.println("refund_detail spec_nos: " + StringUtils.join(refundOrderDetailSpecNos, ","));
		}
		System.out.println(
				"total :" + response.getTotal() + " first refund_no: " + response.getOrders().get(0).getRefundNo());
	}
}
