package com.sdk.tms.baohong.utils;

import com.sdk.tms.baohong.api.asn.ServiceForAsn;
import com.sdk.tms.baohong.api.asn.ServiceForAsn_Service;
import com.sdk.tms.baohong.api.order.ErrorType;
import com.sdk.tms.baohong.api.order.HeaderRequest;
import com.sdk.tms.baohong.api.order.ServiceForOrder;
import com.sdk.tms.baohong.api.order.ServiceForOrder_Service;
import com.sdk.tms.baohong.api.product.ServiceForProduct;
import com.sdk.tms.baohong.api.product.ServiceForProduct_Service;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author liuruipeng
 * 保宏工具类
 * @date 2024年01月20日 18:06
 */
public class BaoHongUtils {

    public static HeaderRequest getOrderHeader(){
        HeaderRequest headerRequest = new HeaderRequest();
        headerRequest.setAppKey("98f8fd9bb9edfa770bc0a317b8203fc3");
        headerRequest.setAppToken("BAAC60E49804C53A");
        headerRequest.setCustomerCode("E0207");
        return headerRequest;
    }

    public static com.sdk.tms.baohong.api.product.HeaderRequest getProductHeader(){
        com.sdk.tms.baohong.api.product.HeaderRequest headerRequest = new com.sdk.tms.baohong.api.product.HeaderRequest();
        headerRequest.setAppKey("98f8fd9bb9edfa770bc0a317b8203fc3");
        headerRequest.setAppToken("BAAC60E49804C53A");
        headerRequest.setCustomerCode("E0207");
        return headerRequest;
    }

    public static com.sdk.tms.baohong.api.asn.HeaderRequest getAsnHeader(){
        com.sdk.tms.baohong.api.asn.HeaderRequest headerRequest = new com.sdk.tms.baohong.api.asn.HeaderRequest();
        headerRequest.setAppKey("98f8fd9bb9edfa770bc0a317b8203fc3");
        headerRequest.setAppToken("BAAC60E49804C53A");
        headerRequest.setCustomerCode("E0207");
        return headerRequest;
    }

    public static ServiceForOrder getOrderService(){
        ServiceForOrder_Service service = new ServiceForOrder_Service();
        return service.getServiceForOrderSOAP();
    }

    public static ServiceForProduct getProductService(){
        ServiceForProduct_Service service = new ServiceForProduct_Service();
        return service.getServiceForProductSOAP();
    }

    public static ServiceForAsn getAsnService(){
        ServiceForAsn_Service service = new ServiceForAsn_Service();
        return service.getServiceForAsnSOAP();
    }

    public static <T> BaoHongResponse<T> buildBaseResponse(Holder<String> askHolder, Holder<String> messageHolder,T data){
        BaoHongResponse<T> response = new BaoHongResponse<>();
        response.setAsk(askHolder.value);
        response.setMessage(messageHolder.value);
        response.setData(data);
        return response;
    }

    public static <T> BaoHongResponse<T> buildBaseResponse(Holder<String> askHolder, Holder<String> messageHolder, Holder<List<ErrorType>> error,T data){
        BaoHongResponse<T> response = new BaoHongResponse<>();
        response.setAsk(askHolder.value);
        response.setMessage(messageHolder.value+error.value.toString());
        response.setData(data);
        return response;
    }
}
