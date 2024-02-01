package com.erp.tms.aliexpress.service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.tms.aliexpress.api.IopClient;
import com.erp.tms.aliexpress.api.IopClientImpl;
import com.erp.tms.aliexpress.api.IopRequest;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.constants.PathConstants;
import com.erp.tms.aliexpress.domain.Protocol;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import com.erp.tms.aliexpress.model.handover.request.*;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 速卖通交接服务层
 */
@Component
public class AliExpressHandoverService {

    /**
     * 查询大包详情
     * @param authMap
     * @param handoverQueryRequest
     * @return HandoverQueryResponse
     * @throws ApiException
     */
    public IopResponse query(Map<String, String> authMap, HandoverQueryRequest handoverQueryRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.content.query");
        request.addApiParameter("user_info", JSONObject.toJSONString(handoverQueryRequest.getUserInfo()));
        request.addApiParameter("order_code", handoverQueryRequest.getOrderCode());
        request.addApiParameter("tracking_number", handoverQueryRequest.getTrackingNumber());
        request.addApiParameter("client", handoverQueryRequest.getClient());
        request.addApiParameter("locale", handoverQueryRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    public IopResponse commit(Map<String, String> authMap, CommitRequest commitRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.commit");
        request.addApiParameter("seller_parcel_order_list", JSONObject.toJSONString(commitRequest.getSellerParcelOrderList()));
        request.addApiParameter("skip_invalid_parcel", String.valueOf(commitRequest.getSkipInvalidParcel()));
        request.addApiParameter("remark", commitRequest.getRemark());
        request.addApiParameter("return_info", JSONObject.toJSONString(commitRequest.getReturnInfo()));
        request.addApiParameter("pickup_info", JSONObject.toJSONString(commitRequest.getPickInfo()));
        request.addApiParameter("order_code_list", String.join(",", commitRequest.getOrderCodeList()));
        request.addApiParameter("weight", String.valueOf(commitRequest.getWeight()));
        request.addApiParameter("handover_order_id", commitRequest.getHandoverOrderId());
        request.addApiParameter("user_info", JSONObject.toJSONString(commitRequest.getUserInfo()));
        request.addApiParameter("weight_unit", commitRequest.getWeightUnit());
        request.addApiParameter("type", commitRequest.getType());
        request.addApiParameter("client", commitRequest.getClient());
        request.addApiParameter("locale", commitRequest.getLocale());
        request.addApiParameter("features", JSONObject.toJSONString(commitRequest.getFeatures()));
        request.addApiParameter("appointment_type", commitRequest.getAppointmentType());
        request.addApiParameter("domestic_tracking_no", commitRequest.getDomesticTrackingNo());
        request.addApiParameter("domestic_logistics_company_id", commitRequest.getDomesticLogisticsCompanyId());
        request.addApiParameter("domestic_logistics_company", commitRequest.getDomesticLogisticsCompany());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    public IopResponse update(Map<String, String> authMap, UpdateRequest updateRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.update");
        request.addApiParameter("weight", String.valueOf(updateRequest.getWeight()));
        request.addApiParameter("weight_unit", updateRequest.getWeightUnit());
        request.addApiParameter("handover_order_id", updateRequest.getHandoverOrderId());
        request.addApiParameter("user_info", JSONObject.toJSONString(updateRequest.getUserInfo()));
        request.addApiParameter("remark", updateRequest.getRemark());
        request.addApiParameter("return_info", JSONObject.toJSONString(updateRequest.getReturnInfo()));
        request.addApiParameter("pickup_info", JSONObject.toJSONString(updateRequest.getPickInfo()));
        request.addApiParameter("order_code_list", String.join(",", updateRequest.getOrderCodeList()));
        request.addApiParameter("type", updateRequest.getType());
        request.addApiParameter("client", updateRequest.getClient());
        request.addApiParameter("locale", updateRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }
    public IopResponse cancel(Map<String, String> authMap, CancelRequest cancelRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.cancel");
        request.addApiParameter("tracking_number", cancelRequest.getTrackingNumber());
        request.addApiParameter("handover_order_id", cancelRequest.getHandoverOrderId());
        request.addApiParameter("user_info", JSONObject.toJSONString(cancelRequest.getUserInfo()));
        request.addApiParameter("handover_content_id", cancelRequest.getHandoverContentId());
        request.addApiParameter("client", cancelRequest.getClient());
        request.addApiParameter("locale", cancelRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    public IopResponse cloudPrint(Map<String, String> authMap, CloudPrintRequest cloudPrintRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.cloudprint.get");
        request.addApiParameter("tracking_number", cloudPrintRequest.getTrackingNumber());
        request.addApiParameter("order_code", cloudPrintRequest.getOrderCode());
        request.addApiParameter("user_info", JSONObject.toJSONString(cloudPrintRequest.getUserInfo()));
        request.addApiParameter("client", cloudPrintRequest.getClient());
        request.addApiParameter("locale", cloudPrintRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }
    private void validate(String appKey,String appSecret,String token,String url){
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(token) || StringUtils.isBlank(url) ) throw new ServiceException("授权信息不能为空");
    }

    public static void main(String[] args) throws ApiException {
        Map<String, String> authMap = new HashMap<>();
        authMap.put("clientId", PathConstants.APP_KEY);
        authMap.put("clientSecret", PathConstants.APP_SECRET);
        authMap.put("token", PathConstants.TOKEN);
        authMap.put("url", PathConstants.BASE_URL);
        authMap.put("code", "3_502978_YNyPkGTynMfcDkZYcOXO58qf299");
        AliExpressHandoverService service = new AliExpressHandoverService();
        //授权
//        JSONObject jsonObject = service.generateToken(authMap);
//        System.out.println(jsonObject);
        //大包查询
        HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
                .client(PathConstants.CLIENT)
                .locale("zh_CN")
                .orderCode("8183868002476390")
                .trackingNumber("LP00629346935157")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse query = service.query(authMap, handoverQueryRequest);
        System.out.println(query);
    }

    public JSONObject generateToken(Map<String, String> map) throws ApiException {
        String appKey = map.getOrDefault("clientId", "");
        String appSecret = map.getOrDefault("clientSecret", "");
        String code = map.getOrDefault("code", "");
        String baseUrl = map.getOrDefault("url", "");
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("/auth/token/create");
        request.addApiParameter("code", code);
        IopResponse response = client.execute(request, Protocol.GOP);
        return JSONObject.parseObject(response.getBody());
    }
}
