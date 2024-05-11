package cn.wangdian.erp.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.api.purchasereturn.PurchaseReturnAPI;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderRequest;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

public class PurchaseReturn 
{
	
	public static void main(String[] args) 
	{
		Client client = DefaultClient.get("wdterp30", "http://192.168.137.1:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
		
		PurchaseReturnAPI purchaseReturnAPI = ApiFactory.get(client, PurchaseReturnAPI.class);
		
		PurchaseReturnCreateOrderRequest.PurchaseReturnOrderInfo orderInfo = new PurchaseReturnCreateOrderRequest.PurchaseReturnOrderInfo();
		orderInfo.setOuterNo("CxxR201501010004");
		orderInfo.setWarehouseNo("lj_test3");
		orderInfo.setProviderNo("1001");
		orderInfo.setPostFee(new BigDecimal(5));
		orderInfo.setOtherFee(new BigDecimal(10));
		orderInfo.setRemark("lichTest");
		orderInfo.setProvince(110000);
		orderInfo.setCity(110100);
		orderInfo.setDistrict(110103);

		PurchaseReturnCreateOrderRequest.PurchaseReturnDetail detail = new PurchaseReturnCreateOrderRequest.PurchaseReturnDetail();
		detail.setSpecNo("4565656565");
		detail.setDiscount(new BigDecimal(5));
		detail.setNum(new BigDecimal(5));
		detail.setPrice(new BigDecimal(5));
		detail.setRemark("test");
		
		List<PurchaseReturnCreateOrderRequest.PurchaseReturnDetail> detailList = new ArrayList<>();
		detailList.add(detail);
		boolean isSubmit = false;
		
		PurchaseReturnCreateOrderResponse response = purchaseReturnAPI.createOrder(orderInfo, detailList, isSubmit);
		System.out.println(response.getMessage());
	}
}
