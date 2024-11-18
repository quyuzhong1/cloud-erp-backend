package com.erp.oms.aliexpress.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.request.AddressRequest;
import com.erp.oms.aliexpress.dto.request.CommonRequest;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.*;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.tms.aliexpress.model.query.request.QueryShipmentOrder;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 速卖通订单服务
 *
 * @author yl
 * @date 2023-11-22
 */
@Slf4j
@Component
public class AliExpressOrderService {

    public static final Integer aliExpressPageSize = 50;

    public static final String ALIEXPRESS_TIME_ZONE = "America/Tijuana";

    private static RedisUtil redisUtil;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        AliExpressOrderService.redisUtil = redisUtil;
    }
    /**
     * 拉取订单
     *
     * @author yl
     * @date 2023-11-22
     */
    @Deprecated
    public void listOrder(OrderRequest orderRequest, List<AliExpressOrder> allOrderList) throws ApiException {
        String appKey = orderRequest.getClientId();
        String appSecret = orderRequest.getClientSecret();
        String baseUrl = orderRequest.getBaseUrl();
        String apiName = orderRequest.getApiName();
        Integer currentPage = orderRequest.getCurrentPage();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("current_page", orderRequest.getCurrentPage());
        paramMap.put("page_size", aliExpressPageSize);

        paramMap.put("create_date_start", orderRequest.getCreateDateStart());
        paramMap.put("create_date_end", orderRequest.getCreateDateEnd());
        if (StringUtils.isNotBlank(orderRequest.getStartTime())){
            paramMap.put("modified_date_start", orderRequest.getStartTime());
        }
        if (StringUtils.isNotBlank(orderRequest.getEndTime())){
            paramMap.put("modified_date_end", orderRequest.getEndTime());
        }
        if (StringUtils.isNotBlank(orderRequest.getOrderStatus())){
            paramMap.put("order_status", orderRequest.getOrderStatus());
        }
        if (CollectionUtils.isNotEmpty(orderRequest.getOrderStatusList())){
            paramMap.put("order_status_list", orderRequest.getOrderStatusList());
        }

        request.addApiParameter("simplify", "true");
        request.addApiParameter("param_aeop_order_query", JSONUtil.toJsonStr(paramMap));
        String token = orderRequest.getToken();
        IopResponse response = client.execute(request, token, Protocol.TOP);
        log.info("拉取速卖通订单>>>>>>>请求={}， 响应={}",JSONUtil.toJsonStr(request), JSONUtil.toJsonStr(response));
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("result");
        if (null == resultJsONObject) {
            String msg = StrUtil.format("拉取速卖通订单失败:无result, response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        Boolean success = resultJsONObject.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("拉取速卖通订单失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            String msg = StrUtil.format("拉取速卖通订单失败:response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        //目录列表
        List<AliExpressOrder> orderInfoList = resultJsONObject.getBeanList("target_list", AliExpressOrder.class);
        if (CollectionUtils.isEmpty(orderInfoList)) {
            return;
        }
//        for (AliExpressOrder item : orderInfoList) {
//            //订单id
//            String orderId = item.getOrderId();
//            AliExpressOrderDetail orderDetail = this.getOrderDetail(orderId, orderRequest);
//            if (Objects.nonNull(orderDetail)) {
//                item.setDetail(orderDetail);
//            }
//        }
        allOrderList.addAll(orderInfoList);
        //总页数
        Integer totalPage = resultJsONObject.getInt("total_page", 0);
        //表示还有
        if (Objects.nonNull(totalPage) && !totalPage.equals(currentPage)) {
            orderRequest.setCurrentPage(currentPage + 1);
            listOrder(orderRequest, allOrderList);
        }

    }

    /**
     * 拉取所有订单
     */
    public List<AliExpressOrder> allOrder(OrderRequest orderRequest) throws ApiException{
        // 分页请求
        JSONObject resultJsONObject = pageOrder(orderRequest);

        //目录列表
        List<AliExpressOrder> orderInfoList = resultJsONObject.getBeanList("target_list", AliExpressOrder.class);
        if (CollectionUtils.isEmpty(orderInfoList)) {
            return Collections.emptyList();
        }
        // 所有订单
        List<AliExpressOrder> allOrderList = new ArrayList<>(orderInfoList);
        //总页数
        Integer totalPage = resultJsONObject.getInt("total_page", 0);
        // 反向分页查询
        for (Integer page = totalPage; page > 1; page--) {
            orderRequest.setCurrentPage(page);
            JSONObject curJSONObjet = pageOrder(orderRequest);
            List<AliExpressOrder> curOrderInfoList = curJSONObjet.getBeanList("target_list", AliExpressOrder.class);
            allOrderList.addAll(curOrderInfoList);
        }
        return allOrderList;
    }


    /**
     * 拉取订单
     */
    public JSONObject pageOrder(OrderRequest orderRequest) throws ApiException {
        String appKey = orderRequest.getClientId();
        String appSecret = orderRequest.getClientSecret();
        String baseUrl = orderRequest.getBaseUrl();
        String apiName = orderRequest.getApiName();
        Integer currentPage = orderRequest.getCurrentPage();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("current_page", orderRequest.getCurrentPage());
        paramMap.put("page_size", aliExpressPageSize);

        paramMap.put("create_date_start", orderRequest.getCreateDateStart());
        paramMap.put("create_date_end", orderRequest.getCreateDateEnd());
        if (StringUtils.isNotBlank(orderRequest.getStartTime())){
            paramMap.put("modified_date_start", orderRequest.getStartTime());
        }
        if (StringUtils.isNotBlank(orderRequest.getEndTime())){
            paramMap.put("modified_date_end", orderRequest.getEndTime());
        }
        if (StringUtils.isNotBlank(orderRequest.getOrderStatus())){
            paramMap.put("order_status", orderRequest.getOrderStatus());
        }
        if (CollectionUtils.isNotEmpty(orderRequest.getOrderStatusList())){
            paramMap.put("order_status_list", orderRequest.getOrderStatusList());
        }
        request.addApiParameter("simplify", "true");
        request.addApiParameter("param_aeop_order_query", JSONUtil.toJsonStr(paramMap));
        String token = orderRequest.getToken();
        IopResponse response = client.execute(request, token, Protocol.TOP);
        log.info("拉取速卖通订单>>>>>>>请求={}， 响应={}",JSONUtil.toJsonStr(request), JSONUtil.toJsonStr(response));
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("result");
        if (null == resultJsONObject) {
            String msg = StrUtil.format("拉取速卖通订单失败:无result, response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        Boolean success = resultJsONObject.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("拉取速卖通订单失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            String msg = StrUtil.format("拉取速卖通订单失败:response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        return resultJsONObject;

    }

    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     * @author yl
     * @date 2023-12-01 15:31
     */
    public AliExpressOrderDetail getOrderDetail(String orderId, OrderRequest orderRequest) throws ApiException {
        String appKey = orderRequest.getClientId();
        String appSecret = orderRequest.getClientSecret();
        String baseUrl = orderRequest.getBaseUrl();
        String apiName = AliexpressConstants.ORDER_DETAIL;
        String token = orderRequest.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        request.addApiParameter("simplify", "true");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("order_id", orderId);
        request.addApiParameter("param1", JSONUtil.toJsonStr(paramMap));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        //成功
        if (jsonObject.containsKey("target")) {
            AliExpressOrderDetail detail = jsonObject.get("target", AliExpressOrderDetail.class);
            return detail;
        }
        String msg = StrUtil.format("拉取速卖通订单明细异常:orderId={}, response={}",orderId, JSONUtil.toJsonStr(response));
        throw new ServiceException(msg);
    }


    /**
     * 获取基础信息
     *
     * @param shopId listOrder
     * @return
     * @author yl
     * @date 2023-11-29 12:13
     */
    public AliExpressShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.ALI_EXPRESS.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof AliExpressShopInfoDTO) {
                return (AliExpressShopInfoDTO) tokenObj;
            }
        }
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                String msg = StrUtil.format("速卖通获取授权信息为空:{}", JSONUtil.toJsonStr(findDTO));
                throw new ServiceException(msg);
            }
            AliExpressShopInfoDTO result = new AliExpressShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());
            result.setId(shopId);
            if (Objects.nonNull(shopAuthEntity)) {
                result.setToken(shopAuthEntity.getToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }

            return result;
    }


    /**
     * 申明发货
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-12-01
     */
    public void declareDeliver(DeclareDeliverRequest declareDeliverRequest) throws ApiException {

        String shopId = declareDeliverRequest.getShopId();
        String shopName = declareDeliverRequest.getShopName();
        AliExpressShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        if (Objects.isNull(shopInfoDTO)) {
            log.error("[速卖通订单声明发货  获取 token 失败: shopId={}", shopId);
            throw new ServiceException(ApiError.ERROR_SHOP_TOKEN_IS_NULL, shopName);
        }
        String appKey = shopInfoDTO.getClientId();
        String appSecret = shopInfoDTO.getClientSecret();
        String baseUrl = shopInfoDTO.getBaseUrl();
        String apiName = AliexpressConstants.DECLARE_DELIVER;
        String token = shopInfoDTO.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.addApiParameter("simplify", "true");
        request.addApiParameter("logistics_no", declareDeliverRequest.getLogisticsNo());
        request.addApiParameter("send_type", declareDeliverRequest.getSendType());
        request.addApiParameter("out_ref", declareDeliverRequest.getOutRef());
        request.addApiParameter("service_name", declareDeliverRequest.getServiceName());
        request.setApiName(apiName);
        log.warn("【{}】速卖通标记发货:请求参数={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(request));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        log.warn("【{}】速卖通标记发货:响应结果={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(response));
        String body = response.getBody();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        Boolean success = jsonObject.getBool("result_success", Boolean.FALSE);
        if (!success) {
            String msg = jsonObject.getOrDefault("result_error_desc", "").toString();
            throw new ServiceException(ApiError.DEFAULT, msg);
        }

    }


    /**
     * 下载地址信息 因地址信息加密了
     *
     * @return
     */
    public BuyerTradeAddress getBuyerTradeAddress(AddressRequest addressRequest) throws ApiException {
        String appKey = addressRequest.getClientId();
        String appSecret = addressRequest.getClientSecret();
        String baseUrl = addressRequest.getBaseUrl();
        String token = addressRequest.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        String apiName = AliexpressConstants.ADDRESS;
        request.setApiName(apiName);
        request.addApiParameter("simplify", "true");
        request.addApiParameter("orderId", addressRequest.getOrderId());
        request.addApiParameter("oaid", addressRequest.getOaid());
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        String code = jsonObject.getOrDefault("code", "").toString();
        //成功
        if (jsonObject.containsKey("result_obj")) {
            BuyerTradeAddress address = jsonObject.get("result_obj", BuyerTradeAddress.class);
            return address;
        }
        return null;
    }

    /**
     * 查询发货单
     * @Author Luo_WG
     * @Date 2024/1/30 14:48
     * @param orderRequest
     * @param orderIdList
     * @return void
     **/
    public List<ErpFulfillmentForwardDtoBean> listDeliveryQuery(OrderRequest orderRequest, List<String> orderIdList) {
        String appKey = orderRequest.getClientId();
        String appSecret = orderRequest.getClientSecret();
        String baseUrl = orderRequest.getBaseUrl();
        String apiName = orderRequest.getApiName();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("customer_order_number_list", orderIdList);
        request.addApiParameter("fulfillment_forward_order_query", com.alibaba.fastjson.JSONObject.toJSONString(paramMap));
        String token = orderRequest.getToken();
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            String jsonStr = JSONUtil.toJsonStr(response);
            log.error("查询速卖通发货单请求失败>>>>>>> response={}, error={}", jsonStr, ExceptionUtil.stacktraceToString(e));
            String msg = StrUtil.format("查询速卖通发货单请求失败>>>>>>>response={}, error={}", jsonStr, ExceptionUtil.stacktraceToString(e));
            throw new ServiceException(msg);
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("aliexpress_ascp_ffo_query_response");
        JSONObject resultJson = JSONUtil.parseObj(resultJsONObject.get("result"));
        Boolean success = resultJson.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("查询速卖通发货单失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            return Collections.emptyList();
        }
        AliExpressAscpFfoQueryResponse result = com.alibaba.fastjson.JSONObject.parseObject(response.getBody(), AliExpressAscpFfoQueryResponse.class);
        DataListBean dataList = result.getAliexpressAscpFfoQueryResponse().getResult().getDataList();
        if (ObjectUtil.isEmpty(dataList) || CollectionUtils.isEmpty(dataList.getErpFulfillmentForwardDto())) {
            return Collections.emptyList();
        }
        return dataList.getErpFulfillmentForwardDto();
    }

    public List<AliExpressDeliveryDetail> listDeliveryDetailQuery(OrderRequest deliveryRequest, String fulfillmentOrderNo) {
        if(StringUtils.isBlank(fulfillmentOrderNo)){
            return new ArrayList<>();
        }
        String appKey = deliveryRequest.getClientId();
        String appSecret = deliveryRequest.getClientSecret();
        String baseUrl = deliveryRequest.getBaseUrl();
        String apiName = AliexpressConstants.ALIEXPRESS_ASCP_FFO_ITEM_QUERY;
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("fulfillment_order_no", fulfillmentOrderNo);
        request.addApiParameter("fulfillment_forward_order_item_query", com.alibaba.fastjson.JSONObject.toJSONString(paramMap));
        String token = deliveryRequest.getToken();
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            String jsonStr = JSONUtil.toJsonStr(response);
            log.error("查询速卖通发货单明细请求失败>>>>>>> response={}, error={}", jsonStr, ExceptionUtil.stacktraceToString(e));
            String msg = StrUtil.format("查询速卖通发货单明细请求失败>>>>>>>response={}, error={}", jsonStr, ExceptionUtil.stacktraceToString(e));
            throw new ServiceException(msg);
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("aliexpress_ascp_ffo_item_query_response");
        JSONObject resultJson = JSONUtil.parseObj(resultJsONObject.get("result"));
        JSONObject dataListJson = JSONUtil.parseObj(resultJson.get("data_list"));
        Boolean success = resultJson.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("查询速卖通发货单明细失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            String msg = StrUtil.format("查询速卖通发货单明细请求失败>>>>>>>response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        List<AliExpressDeliveryDetail> detailList = dataListJson.getBeanList("data",AliExpressDeliveryDetail.class);
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        return detailList;
    }

    /**
     * 子订单声明发货
     */
    public void subDeclareDeliver(DeclareDeliverRequest declareDeliverRequest) throws ApiException {
        String shopId = declareDeliverRequest.getShopId();
        AliExpressShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(shopId);
        if (Objects.isNull(shopInfoDTO)) {
            log.error("[速卖通子订单声明发货  获取 token 失败: shopId={}", shopId);
            throw new ServiceException(ApiError.ERROR_SHOP_TOKEN_IS_NULL, shopId);
        }
        // 组合请求参数
        QueryShipmentOrder.Shipment shipment = QueryShipmentOrder.Shipment.builder()
                .logistics_no(declareDeliverRequest.getLogisticsNo())
                .service_name(declareDeliverRequest.getServiceName())
                .build();
        if (StringUtils.isNotBlank(declareDeliverRequest.getActualCarrier())){
            shipment.setActual_carrier(declareDeliverRequest.getActualCarrier());
        }
        if (StringUtils.isNotBlank(declareDeliverRequest.getTrackingWebSite())){
            shipment.setTracking_web_site(declareDeliverRequest.getTrackingWebSite());
        }
        if (CollectionUtils.isEmpty(declareDeliverRequest.getSubTradeOrderIndexList())){
            ServiceException.runError("提交的子订单小标不能为空");
        }
        List<QueryShipmentOrder.SubTradeOrder> subTradeOrders = new LinkedList<>();
        for (String subOrderIndex : declareDeliverRequest.getSubTradeOrderIndexList()) {
            QueryShipmentOrder.SubTradeOrder tradeOrder = QueryShipmentOrder.SubTradeOrder.builder()
                    .send_type(declareDeliverRequest.getSendType())
                    .sub_trade_order_index(subOrderIndex)
                    .shipment_list(Collections.singletonList(shipment))
                    .build();
            subTradeOrders.add(tradeOrder);
        }

        QueryShipmentOrder requestParams = QueryShipmentOrder.builder()
                .trade_order_id(declareDeliverRequest.getOutRef())
                .sub_trade_order_list(subTradeOrders)
                .build();

        String appKey = shopInfoDTO.getClientId();
        String appSecret = shopInfoDTO.getClientSecret();
        String baseUrl = shopInfoDTO.getBaseUrl();
        String apiName = AliexpressConstants.SUB_DECLARE_DELIVER;
        String token = shopInfoDTO.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);

        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        request.addApiParameter("param_aeop_seller_shipment_sub_trade_order_request", JSONUtil.toJsonStr(requestParams));
        log.warn("【{}】速卖通子声明标记发货:请求参数={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(request));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        log.warn("【{}】速卖通子声明标记发货:响应结果={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(response));
        String body = response.getBody();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        JSONObject resultJsONObject = jsonObject.getJSONObject("aliexpress_logistics_order_shipment_response");
        JSONObject resultJson = JSONUtil.parseObj(resultJsONObject.get("result"));
        boolean success = resultJson.getBool("success", Boolean.FALSE);
        if (!success){
            String errorMsg = resultJson.getStr("error_msg", "");
            Integer errorCode = resultJson.getInt("error_code", -1000000);
            if (StringUtils.isNotBlank(errorMsg)){
                ServiceException.runError(errorCode, errorMsg);
            } else {
                ServiceException.runError(errorCode, JSONUtil.toJsonStr(body));
            }
        }
    }

    public List<JSONObject> carrierQuerylist(CommonRequest commonRequest) {
        IopClient client = new IopClientImpl(commonRequest.getBaseUrl(), commonRequest.getClientId(), commonRequest.getClientSecret());
        IopRequest request = new IopRequest();
        request.setApiName(commonRequest.getApiName());
        String token = commonRequest.getToken();
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            String jsonStr = JSONUtil.toJsonStr(response);
            log.error("查询速卖通所有的实际承运商>>>>>>> response={}, error={}", jsonStr, ExceptionUtil.stacktraceToString(e));
            String msg = StrUtil.format("查询速卖通发货单请求失败>>>>>>>response={}, error={}", jsonStr, ExceptionUtil.stacktraceToString(e));
            throw new ServiceException(msg);
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("aliexpress_ascp_ffo_query_response");
        JSONObject resultJson = JSONUtil.parseObj(resultJsONObject.get("result"));
        Boolean success = resultJson.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("查询速卖通所有的实际承运商失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            return Collections.emptyList();
        }
        return null;
//        AliExpressAscpFfoQueryResponse result = JSONObject.parseObject(response.getBody(), AliExpressAscpFfoQueryResponse.class);
//        DataListBean dataList = result.getAliexpressAscpFfoQueryResponse().getResult().getDataList();
//        if (ObjectUtil.isEmpty(dataList) || CollectionUtils.isEmpty(dataList.getErpFulfillmentForwardDto())) {
//            return Collections.emptyList();
//        }
//        return dataList.getErpFulfillmentForwardDto();
    }



        public static void main1(String[] args) throws Exception {
//        String appKey = "502978";
//        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
//        String baseUrl = "https://api-sg.aliexpress.com";
//        String apiName = AliexpressConstants.ORDER_DETAIL;
//        String token = "50000200123dJAvRobgSKEtBJjvZtxEAZfV17b52f96gJQg0OG9CCvBqT1l8Mocp35cG";
//        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
//        IopRequest request = new IopRequest();
//        Map<String,String> map=new HashMap<>();
//        map.put("order_id","5359358739744155");
//        request.addApiParameter("simplify", "true");
//        request.addApiParameter("param1",JSONUtil.toJsonStr(map));
//        request.setApiName(apiName);
//        IopResponse response = client.execute(request, token, Protocol.TOP);
//        String body = response.getBody();
//        System.out.println(body);
        OrderRequest orderRequest = OrderRequest.builder()
                .clientId("502978")
                .clientSecret("DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY")
                .baseUrl("https://api-sg.aliexpress.com")
                .apiName(AliexpressConstants.LIST_ORDER)
                .currentPage(1)
//                .orderStatus("FINISH")
                .orderStatusList(Arrays.asList("PLACE_ORDER_SUCCESS", "IN_CANCEL", "WAIT_SELLER_SEND_GOODS", "SELLER_PART_SEND_GOODS", "WAIT_BUYER_ACCEPT_GOODS", "FUND_PROCESSING", "IN_FROZEN", "IN_ISSUE", "WAIT_SELLER_EXAMINE_MONEY", "RISK_CONTROL", "FINISH"))
//                .orderStatusList(Arrays.asList("FINISH"))
                .token("50000200123dJAvRobgSKEtBJjvZtxEAZfV17b52f96gJQg0OG9CCvBqT1l8Mocp35cG")
//                .startTime("2024-05-10 00:00:00")
//                .endTime("2024-05-16 00:00:00")
                .createDateStart("2024-05-17 00:00:00")
                .createDateEnd("2024-05-18 00:00:00")
                .build();
//        OrderRequest(clientId=502978, clientSecret=DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY,
//                baseUrl=https://api-sg.aliexpress.com, apiName=aliexpress.trade.seller.orderlist.get,
        // startTime=2024-05-10 00:00:00,
        // endTime=2024-05-16 00:00:00,
        // token=50000700312cJ4nYbrzErAqH159364aboXoPdAcJwjMuEzrGXEAudlrVcjGsjo4bIr30, orderStatus=,
        // orderStatusList=["FINISH"], currentPage=2,
        // createDateStart=2024-02-10 00:00:00,
        // createDateEnd=2024-06-21 14:12:08)


        AliExpressOrderService aliExpressOrderService = new AliExpressOrderService();
        List<AliExpressOrder> aliExpressOrders = aliExpressOrderService.allOrder(orderRequest);
//        ArrayList<AliExpressOrder>  aliExpressOrders = new ArrayList<>();
//        aliExpressOrderService.listOrder(orderRequest, aliExpressOrders);
        System.out.println(aliExpressOrders);
        System.out.println("数量" + aliExpressOrders.size());
    }


    /**
     * 根据店铺ID和订单ID查询订单详情
     */
    public AliExpressOrderDetail getOrderDetailByOrderIdAndShopId(String platformCode, String shopId) {
        String apiName = AliexpressConstants.LIST_ORDER;
        AliExpressShopInfoDTO shopInfoDTO = getShopInfoByShopId(shopId);
        if (null == shopInfoDTO) {
            log.error("[速卖通订单明细下载]  获取 token 失败: shopId={}", shopId);
            String msg = StrUtil.format("[速卖通订单下载]  获取 token 失败: shopId={}", shopId);
            throw new ServiceException(msg);
        }
        OrderRequest orderRequest = OrderRequest.builderByShopInfo(apiName, shopInfoDTO);
        try {
            return getOrderDetail(platformCode, orderRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }
}
