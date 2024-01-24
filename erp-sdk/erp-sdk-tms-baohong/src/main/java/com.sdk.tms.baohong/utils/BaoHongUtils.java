package com.sdk.tms.baohong.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.baohong.api.asn.ServiceForAsn;
import com.sdk.tms.baohong.api.asn.ServiceForAsn_Service;
import com.sdk.tms.baohong.api.order.ErrorType;
import com.sdk.tms.baohong.api.order.HeaderRequest;
import com.sdk.tms.baohong.api.order.ServiceForOrder;
import com.sdk.tms.baohong.api.order.ServiceForOrder_Service;
import com.sdk.tms.baohong.api.product.ServiceForProduct;
import com.sdk.tms.baohong.api.product.ServiceForProduct_Service;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import okhttp3.Call;
import okhttp3.Response;

import javax.xml.ws.Holder;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuruipeng
 * 保宏工具类
 * @date 2024年01月20日 18:06
 */
public class BaoHongUtils {

    private static final String printUrl = "http://exwms.globex.cn/default/print-order-api/print-order";

    public static HeaderRequest getOrderHeader(){
        HeaderRequest headerRequest = new HeaderRequest();
        headerRequest.setAppKey("98f8fd9bb9edfa770bc0a31b8203fc3");
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

    public static BaoHongResponse<String> getPrintLabelBase64(String orderCode) {
        BaoHongResponse<String> result = new BaoHongResponse<>();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("customerCode","E0207");
        paramsMap.put("appKey","98f8fd9bb9edfa770bc0a317b8203fc3");
        paramsMap.put("appToken","BAAC60E49804C53A");
        Map<String,String> dataMap = new HashMap<>();
        dataMap.put("orderCode",orderCode);
        paramsMap.put("data",dataMap);
        Map<String,String> headerMap = new HashMap<>();
        Call call = OkHttpUtils.createPostJsonCall(printUrl,paramsMap,headerMap);
        String base64String = null;
        try (Response response = call.execute()) {
            // 检查响应是否成功
            if (response.isSuccessful()) {
                // 将响应保存为 PDF 文件
                byte[] pdfBytes = response.body().bytes();
                // 将响应的 PDF 内容转换为 BASE64 编码
                base64String = Base64.getEncoder().encodeToString(pdfBytes);
            } else {
                result.setAsk("0");
                result.setMessage("调用保宏打印接口响应失败");
            }
        } catch (IOException e) {
            result.setAsk("0");
            result.setMessage(ExceptionUtil.stacktraceToString(e,500));
        }
        result.setAsk("1");
        result.setData(base64String);
        return result;
    }
}
