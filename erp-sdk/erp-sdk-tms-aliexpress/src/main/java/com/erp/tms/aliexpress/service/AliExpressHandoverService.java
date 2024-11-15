package com.erp.tms.aliexpress.service;

import com.alibaba.fastjson.JSON;
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

import java.util.Map;
import java.util.Objects;

/**
 * 速卖通交接服务层-菜鸟国际出口
 */
@Slf4j
@Component
public class AliExpressHandoverService {
    
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String TOKEN = "token";
    private static final String URL = "url";
    private static final String API_NAME_ADD = "cainiao.global.handover.content.subbag.add";
    private static final String USER_INFO = "user_info";
    private static final String ORDER_CODE = "order_code";
    private static final String ADD_SUBBAG_QUANTITY = "add_subbag_quantity";
    private static final String LOCALE = "locale";
    private static final String SIMPLIFY = "simplify";
    private static final String TRACKING_NUMBER = "tracking_number";
    private static final String CLIENT = "client";
    private static final String PICKUP_INFO = "pickup_info";
    private static final String HANDOVER_ORDER_ID = "handover_order_id";
    
    /**
     * 批次追加大包
     * @param authMap
     * @param subbagRequest
     * @return SubbagResponse
     * @throws ApiException
     */
    public IopResponse subbagAdd(Map<String, String> authMap, SubbagRequest subbagRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(API_NAME_ADD);
        request.addApiParameter(USER_INFO, JSON.toJSONString(subbagRequest.getUserInfo()));
        request.addApiParameter(ORDER_CODE, subbagRequest.getOrderCode());
        request.addApiParameter(ADD_SUBBAG_QUANTITY, String.valueOf(subbagRequest.getAddSubbagQuantity()));
        request.addApiParameter(LOCALE, subbagRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    /**
     * 查询大包详情
     * @param authMap
     * @param handoverQueryRequest
     * @return HandoverQueryResponse
     * @throws ApiException
     */
    public IopResponse queryContent(Map<String, String> authMap, HandoverQueryRequest handoverQueryRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.content.query");
        request.addApiParameter(USER_INFO, JSON.toJSONString(handoverQueryRequest.getUserInfo()));
        request.addApiParameter(ORDER_CODE, handoverQueryRequest.getOrderCode());
        request.addApiParameter(TRACKING_NUMBER, handoverQueryRequest.getTrackingNumber());
        request.addApiParameter(CLIENT, handoverQueryRequest.getClient());
        request.addApiParameter(LOCALE, handoverQueryRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    /**
     * 提供给ISV通过该接口查询小包信息
     * @param authMap
     * @param handoverQueryRequest
     * @return HandoverQueryResponse
     * @throws ApiException
     */
    public IopResponse queryParcel(Map<String, String> authMap, HandoverQueryRequest handoverQueryRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.parcel.query");
        request.addApiParameter(USER_INFO, JSON.toJSONString(handoverQueryRequest.getUserInfo()));
        request.addApiParameter(ORDER_CODE, handoverQueryRequest.getOrderCode());
        request.addApiParameter(TRACKING_NUMBER, handoverQueryRequest.getTrackingNumber());
        request.addApiParameter(CLIENT, handoverQueryRequest.getClient());
        request.addApiParameter(LOCALE, handoverQueryRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
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
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.commit");
        if(CollectionUtils.isNotEmpty(commitRequest.getSellerParcelOrderList())){
            request.addApiParameter("seller_parcel_order_list", JSON.toJSONString(commitRequest.getSellerParcelOrderList()));
        }
        if (Objects.nonNull(commitRequest.getSkipInvalidParcel())){
            request.addApiParameter("skip_invalid_parcel", String.valueOf(commitRequest.getSkipInvalidParcel()));
        }
        request.addApiParameter("remark", commitRequest.getRemark());
        if (Objects.nonNull(commitRequest.getReturnInfo())){
            request.addApiParameter("return_info", JSON.toJSONString(commitRequest.getReturnInfo()));
        }
        request.addApiParameter(PICKUP_INFO, JSON.toJSONString(commitRequest.getPickInfo()));
        request.addApiParameter("order_code_list", String.join(",", commitRequest.getOrderCodeList()));
        request.addApiParameter("weight", String.valueOf(commitRequest.getWeight()));
        request.addApiParameter(HANDOVER_ORDER_ID, commitRequest.getHandoverOrderId());
        request.addApiParameter(USER_INFO, JSON.toJSONString(commitRequest.getUserInfo()));
        request.addApiParameter("weight_unit", commitRequest.getWeightUnit());
        request.addApiParameter("type", commitRequest.getType());
        request.addApiParameter(CLIENT, commitRequest.getClient());
        request.addApiParameter(LOCALE, commitRequest.getLocale());
        if (Objects.nonNull(commitRequest.getFeatures())){
            request.addApiParameter("features", JSON.toJSONString(commitRequest.getFeatures()));
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
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
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
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.update");
        request.addApiParameter("weight", String.valueOf(updateRequest.getWeight()));
        request.addApiParameter("weight_unit", updateRequest.getWeightUnit());
        request.addApiParameter(HANDOVER_ORDER_ID, updateRequest.getHandoverOrderId());
        request.addApiParameter(USER_INFO, JSON.toJSONString(updateRequest.getUserInfo()));
        request.addApiParameter("remark", updateRequest.getRemark());
        request.addApiParameter("return_info", JSON.toJSONString(updateRequest.getReturnInfo()));
        request.addApiParameter(PICKUP_INFO, JSON.toJSONString(updateRequest.getPickInfo()));
        request.addApiParameter("order_code_list", String.join(",", updateRequest.getOrderCodeList()));
        request.addApiParameter("type", updateRequest.getType());
        request.addApiParameter(CLIENT, updateRequest.getClient());
        request.addApiParameter(LOCALE, updateRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
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
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.cancel");
        request.addApiParameter(TRACKING_NUMBER, cancelRequest.getTrackingNumber());
        request.addApiParameter(HANDOVER_ORDER_ID, cancelRequest.getHandoverOrderId());
        request.addApiParameter(USER_INFO, JSON.toJSONString(cancelRequest.getUserInfo()));
        request.addApiParameter("handover_content_id", String.valueOf(cancelRequest.getHandoverContentId()));
        request.addApiParameter(CLIENT, cancelRequest.getClient());
        request.addApiParameter(LOCALE, cancelRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
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
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.cloudprint.get");
        request.addApiParameter(TRACKING_NUMBER, cloudPrintRequest.getTrackingNumber());
        request.addApiParameter(ORDER_CODE, cloudPrintRequest.getOrderCode());
        request.addApiParameter(USER_INFO, JSON.toJSONString(cloudPrintRequest.getUserInfo()));
        request.addApiParameter(CLIENT, cloudPrintRequest.getClient());
        request.addApiParameter(LOCALE, cloudPrintRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    /**
     * 返回指定大包面单的PDF文件数据
     * @param authMap
     * @param pdfRequest
     * @return PdfResponse
     * @throws ApiException
     */
    public IopResponse getPdf(Map<String, String> authMap, PdfRequest pdfRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.handover.pdf.get");
        request.addApiParameter("handover_content_id", String.valueOf(pdfRequest.getHandoverContentId()));
        request.addApiParameter("type", String.valueOf(pdfRequest.getType()));
        request.addApiParameter(USER_INFO, JSON.toJSONString(pdfRequest.getUserInfo()));
        request.addApiParameter(CLIENT, pdfRequest.getClient());
        request.addApiParameter(LOCALE, pdfRequest.getLocale());
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
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
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.pickup.resource.recommend");
        request.addApiParameter("solution_code", resourceRecommendRequest.getSolutionCode());
        request.addApiParameter("pickup_type", resourceRecommendRequest.getPickupType());
        request.addApiParameter(USER_INFO, JSON.toJSONString(resourceRecommendRequest.getUserInfo()));
        request.addApiParameter(PICKUP_INFO, JSON.toJSONString(resourceRecommendRequest.getPickInfo()));
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    /**
     * 查询出所有的实际承运商
     * @param authMap
     * @param locale
     * @return CarrierResponse
     * @throws ApiException
     */
    public IopResponse queryCarrierList(Map<String, String> authMap, String locale) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.global.logistics.carrier.querylist");
        request.addApiParameter(LOCALE, locale);
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
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
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
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
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }
    private void validate(String appKey,String appSecret,String token,String url){
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(token) || StringUtils.isBlank(url) ) throw new ServiceException("授权信息不能为空");
    }

}
