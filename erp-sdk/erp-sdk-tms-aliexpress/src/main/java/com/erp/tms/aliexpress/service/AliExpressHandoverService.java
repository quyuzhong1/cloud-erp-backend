package com.erp.tms.aliexpress.service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.tms.aliexpress.api.IopClient;
import com.erp.tms.aliexpress.api.IopClientImpl;
import com.erp.tms.aliexpress.api.IopRequest;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.constants.PathConstants;
import com.erp.tms.aliexpress.domain.Protocol;
import com.erp.tms.aliexpress.model.handover.request.*;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 速卖通交接服务层-菜鸟国际出口
 */
@Slf4j
@Component
public class AliExpressHandoverService {
    /**
     * 批次追加大包
     * @param authMap
     * @param subbagRequest
     * @return SubbagResponse
     * @throws ApiException
     */
    public IopResponse subbagAdd(Map<String, String> authMap, SubbagRequest subbagRequest) throws ApiException {
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
        request.setApiName("cainiao.global.handover.content.subbag.add");
        request.addApiParameter("user_info", JSONObject.toJSONString(subbagRequest.getUserInfo()));
        request.addApiParameter("order_code", subbagRequest.getOrderCode());
        request.addApiParameter("add_subbag_quantity", String.valueOf(subbagRequest.getAddSubbagQuantity()));
        request.addApiParameter("locale", subbagRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    /**
     * 查询大包详情
     * @param authMap
     * @param handoverQueryRequest
     * @return HandoverQueryResponse
     * @throws ApiException
     */
    public IopResponse queryContent(Map<String, String> authMap, HandoverQueryRequest handoverQueryRequest) throws ApiException {
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

    /**
     * 提供给ISV通过该接口查询小包信息
     * @param authMap
     * @param handoverQueryRequest
     * @return HandoverQueryResponse
     * @throws ApiException
     */
    public IopResponse queryParcel(Map<String, String> authMap, HandoverQueryRequest handoverQueryRequest) throws ApiException {
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
        request.setApiName("cainiao.global.handover.parcel.query");
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

    /**
     * 提供给ISV通过该接口提交发布交接单
     *
     * @param authMap
     * @param commitRequest
     * @return HandoverCommitResponse
     * @throws ApiException
     */
    public IopResponse commit(Map<String, String> authMap, CommitRequest commitRequest) throws ApiException {
        log.info("==========AliExpressHandoverService.commit==========start");
        log.info("authMap:{}, commitRequest:{}",authMap, commitRequest);
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
        if(CollectionUtils.isNotEmpty(commitRequest.getSellerParcelOrderList())){
            request.addApiParameter("seller_parcel_order_list", JSONObject.toJSONString(commitRequest.getSellerParcelOrderList()));
        }
        if (Objects.nonNull(commitRequest.getSkipInvalidParcel())){
            request.addApiParameter("skip_invalid_parcel", String.valueOf(commitRequest.getSkipInvalidParcel()));
        }
        request.addApiParameter("remark", commitRequest.getRemark());
        if (Objects.nonNull(commitRequest.getReturnInfo())){
            request.addApiParameter("return_info", JSONObject.toJSONString(commitRequest.getReturnInfo()));
        }
        request.addApiParameter("pickup_info", JSONObject.toJSONString(commitRequest.getPickInfo()));
        request.addApiParameter("order_code_list", String.join(",", commitRequest.getOrderCodeList()));
        request.addApiParameter("weight", String.valueOf(commitRequest.getWeight()));
        request.addApiParameter("handover_order_id", commitRequest.getHandoverOrderId());
        request.addApiParameter("user_info", JSONObject.toJSONString(commitRequest.getUserInfo()));
        request.addApiParameter("weight_unit", commitRequest.getWeightUnit());
        request.addApiParameter("type", commitRequest.getType());
        request.addApiParameter("client", commitRequest.getClient());
        request.addApiParameter("locale", commitRequest.getLocale());
        if (Objects.nonNull(commitRequest.getFeatures())){
            request.addApiParameter("features", JSONObject.toJSONString(commitRequest.getFeatures()));
        }
        request.addApiParameter("appointment_type", commitRequest.getAppointmentType());
        if (StringUtils.isNotEmpty(commitRequest.getDomesticTrackingNo())){
            request.addApiParameter("domestic_tracking_no", commitRequest.getDomesticTrackingNo());
        }
        if(StringUtils.isNotEmpty(commitRequest.getDomesticLogisticsCompanyId())){
            request.addApiParameter("domestic_logistics_company_id", commitRequest.getDomesticLogisticsCompanyId());
        }
        if (StringUtils.isNotEmpty(commitRequest.getDomesticLogisticsCompany())){
            request.addApiParameter("domestic_logistics_company", commitRequest.getDomesticLogisticsCompany());
        }
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        log.info("==========AliExpressHandoverService.commit==========end");
        log.info("response:{}",response);
        return response;
    }

    /**
     * 提供给ISV通过该接口修改交接单
     *
     * @param authMap
     * @param updateRequest
     * @return boolean
     * @throws ApiException
     */
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

    /**
     * 提供给ISV通过该接口取消交接单
     *
     * @param authMap
     * @param cancelRequest
     * @return boolean
     * @throws ApiException
     */
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
        request.addApiParameter("handover_content_id", String.valueOf(cancelRequest.getHandoverContentId()));
        request.addApiParameter("client", cancelRequest.getClient());
        request.addApiParameter("locale", cancelRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    /**
     * 提供给ISV通过该接口获取面单云打印数据
     *
     * @param authMap
     * @param cloudPrintRequest
     * @return CloudPrintResponse
     * @throws ApiException
     */
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

    /**
     * 返回指定大包面单的PDF文件数据
     * @param authMap
     * @param pdfRequest
     * @return PdfResponse
     * @throws ApiException
     */
    public IopResponse getPdf(Map<String, String> authMap, PdfRequest pdfRequest) throws ApiException {
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
        request.setApiName("cainiao.global.handover.pdf.get");
        request.addApiParameter("handover_content_id", String.valueOf(pdfRequest.getHandoverContentId()));
        request.addApiParameter("type", String.valueOf(pdfRequest.getType()));
        request.addApiParameter("user_info", JSONObject.toJSONString(pdfRequest.getUserInfo()));
        request.addApiParameter("client", pdfRequest.getClient());
        request.addApiParameter("locale", pdfRequest.getLocale());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    /**
     * 揽收资源推荐
     *
     * @param authMap
     * @param resourceRecommendRequest
     * @return ResourceRecommendResponse
     * @throws ApiException
     */
    public IopResponse resourceRecommend(Map<String, String> authMap, ResourceRecommendRequest resourceRecommendRequest) throws ApiException {
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
        request.setApiName("cainiao.global.pickup.resource.recommend");
        request.addApiParameter("solution_code", resourceRecommendRequest.getSolutionCode());
        request.addApiParameter("pickup_type", resourceRecommendRequest.getPickupType());
        request.addApiParameter("user_info", JSONObject.toJSONString(resourceRecommendRequest.getUserInfo()));
        request.addApiParameter("pickup_info", JSONObject.toJSONString(resourceRecommendRequest.getPickInfo()));
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    /**
     * 查询出所有的实际承运商
     * @param authMap
     * @param locale
     * @return CarrierResponse
     * @throws ApiException
     */
    public IopResponse queryCarrierList(Map<String, String> authMap, String locale) throws ApiException {
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
        request.setApiName("cainiao.global.logistics.carrier.querylist");
        request.addApiParameter("locale", locale);
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    /**
     *查询包裹可用物流方案
     * @param authMap
     * @param serviceRequest
     * @return ServiceResponse
     * @throws ApiException
     * 用于异常订单重新发货时获取物流方案，如异常滞留订单
     */
    public IopResponse queryService(Map<String, String> authMap, ServiceRequest serviceRequest) throws ApiException {
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
        request.setApiName("cainiao.global.logistics.service.query");
        request.addApiParameter("trade_order_id", serviceRequest.getTradeOrderId());
        request.addApiParameter("intl_tracking_no", serviceRequest.getIntlTrackingNo());
        request.addApiParameter("out_order_code", serviceRequest.getOutOrderCode());
        request.addApiParameter("reason", serviceRequest.getReason());
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
        authMap.put("code", "3_502978_KLBOdhI2WuO3i9V5DaTT9leN3894");
        AliExpressHandoverService service = new AliExpressHandoverService();
        //授权
        JSONObject jsonObject = service.generateToken(authMap);
        System.out.println(jsonObject);
        //大包查询
//        HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
//                .client(PathConstants.CLIENT)
//                .locale("zh_CN")
//                .orderCode("8183868002476390")
//                .trackingNumber("LP00629346935157")
//                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
//                .build();
//        BaseResult query = service.queryContent(authMap, handoverQueryRequest);
//        System.out.println(query);
        //可用服务
//        ServiceRequest serviceRequest = ServiceRequest.builder()
//                .intlTrackingNo("LP00629346935157")
//                .outOrderCode("8183868002476390")
//                .tradeOrderId("8183868002476390")
//                .reason("dd")
//                .build();
//        IopResponse iopResponse = service.queryService(authMap, serviceRequest);
//        System.out.println(iopResponse);
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
