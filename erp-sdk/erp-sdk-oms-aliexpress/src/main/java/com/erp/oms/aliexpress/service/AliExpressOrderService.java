package com.erp.oms.aliexpress.service;

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
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.*;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static com.erp.oms.aliexpress.constants.AliexpressConstants.pageSize;

/**
 * 速卖通订单服务
 *
 * @author yl
 * @date 2023-11-22
 */
@Slf4j
@Component
public class AliExpressOrderService {

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
     * @return
     * @parms
     * @author yl
     * @date 2023-11-22
     */
    public void listOrder(OrderRequest orderRequest, List<AliExpressOrder> orderList) throws ApiException {

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
        paramMap.put("page_size", pageSize);
        paramMap.put("create_date_start", orderRequest.getStartTime());
        paramMap.put("create_date_end", orderRequest.getEndTime());
        request.addApiParameter("simplify", "true");
        request.addApiParameter("param_aeop_order_query", JSONUtil.toJsonStr(paramMap));
        String token = orderRequest.getToken();
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("result");
        Boolean success = resultJsONObject.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("拉取速卖通订单失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            return;
        }
        //目录列表
        List<AliExpressOrder> orderInfoList = resultJsONObject.getBeanList("target_list", AliExpressOrder.class);
        if (CollectionUtils.isEmpty(orderInfoList)) {
            return;
        }
        for (AliExpressOrder item : orderInfoList) {
            //订单id
            String orderId = item.getOrderId();
            AliExpressOrderDetail orderDetail = this.getOrderDetail(orderId, orderRequest);
            if (Objects.nonNull(orderDetail)) {
                item.setDetail(orderDetail);
            }
        }
        orderList.addAll(orderInfoList);
        //总页数
        Integer totalPage = resultJsONObject.getInt("total_page", 0);
        //表示还有
        if (Objects.nonNull(totalPage) && !totalPage.equals(currentPage)) {
            orderRequest.setCurrentPage(currentPage + 1);
            listOrder(orderRequest, orderInfoList);
        }

    }

    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     * @author yl
     * @date 2023-12-01 15:31
     */
    private AliExpressOrderDetail getOrderDetail(String orderId, OrderRequest orderRequest) throws ApiException {
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

        return null;
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
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
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
        return null;

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
        IopResponse response = client.execute(request, token, Protocol.TOP);
        String body = response.getBody();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        Boolean success = jsonObject.getBool("result_success", Boolean.FALSE);
        if (!success) {
            String msg = jsonObject.getOrDefault("result_error_desc", "").toString();
            throw new ServiceException(ApiError.Default, msg);
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
        request.addApiParameter("customer_order_number_list", com.alibaba.fastjson.JSONObject.toJSONString(paramMap));
        String token = orderRequest.getToken();
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            log.error("查询速卖通发货单请求失败>>>>>>>{}", request.toString());
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("result");
        Boolean success = resultJsONObject.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("查询速卖通发货单失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            return Collections.emptyList();
        }
        AliExpressAscpFfoQueryResponse result = com.alibaba.fastjson.JSONObject.parseObject(response.getBody(), AliExpressAscpFfoQueryResponse.class);
        DataListBean dataList = result.getAliexpressAscpFfoQueryResponse().getResult().getDataList();
        if (ObjectUtil.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        return dataList.getErpFulfillmentForwardDto();
    }

    public static void main(String[] args) throws ApiException {


        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String baseUrl = "https://api-sg.aliexpress.com";
//        String apiName = AliexpressConstants.DECLARE_DELIVER;
//        String token = "500002000383xXYuTpfDpvgviHHR2uUB9yHxEIwiRSF7Dgx9Mz12af849325O8FaLsaz";
//        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
//        IopRequest request = new IopRequest();
//        request.addApiParameter("simplify", "true");
//        request.addApiParameter("country", "123");
//        request.addApiParameter("warehouseCustomerId", "123");
//
//        request.setApiName("/qimen/aliexpress/warehouse/baseinfo/get");
//        IopResponse response = client.execute(request, Protocol.GOP);
//        String body = response.getBody();
//        System.out.println(body);

        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("/qimen/aliexpress/warehouse/baseinfo/get");
        request.addApiParameter("country", "123");
        request.addApiParameter("warehouseCustomerId", "123");
        request.addApiParameter("systemType", "oms");
        request.setHttpMethod("GET");
        IopResponse response = client.execute(request, Protocol.GOP);
        System.out.println(response.getBody());

    }


}
