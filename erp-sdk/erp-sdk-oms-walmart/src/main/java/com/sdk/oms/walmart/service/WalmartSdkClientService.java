package com.sdk.oms.walmart.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.WalmartShipOrderDetailDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.common.core.utils.UUID;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.WalmartShopInfoDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartOrderDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartShipOrderDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.dto.walmart.ship.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 沃尔玛服务
 * @Author Luo_WG
 * @Date 2023/10/16 14:50
 **/
@Slf4j
@Component
public class WalmartSdkClientService {

    public static void main(String[] args) throws Exception {
        String baseUrl = "https://sandbox.walmartapis.com/v3/token";
        String clientId = "2434a35c-7c42-4420-9618-0c179b68a8c2";
        String clientSecret = "AMW5lbVFqG2DMP4DuLezhSkbk4u0JLGUjdFlsrl_p0sagsBkYPPiQhRbEvkE4a6k6KXNKhB--RGlqPKIfhUoV28";
        //获取令牌

        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);
        System.out.println(walmartTokenDTO);
        //请求参数
        HashMap<String, Object> paramMap = new HashMap<>();
        Integer pageSize = 50;


        JobTaskDTO taskDTO = new JobTaskDTO();

        taskDTO.setLastTime(LocalDateTime.parse("2023-12-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        taskDTO.setNextTime(LocalDateTime.parse("2023-12-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        WalmartTokenDTO s = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);
        baseUrl = WalmartStaticKey.baseUrl + "orders";
        StringBuffer sb = new StringBuffer();
        String nextCursor = "";
        while(true) {
            sb.setLength(0);
            sb.append(baseUrl);
            if (StringUtil.isBlank(nextCursor)) {
                sb.append("?status=Acknowledged,Shipped,Delivered,Cancelled");
/*                sb.append("&lastModifiedStartDate=");
                sb.append(data.getLastTime());
                sb.append("&lastModifiedEndDate=");
                sb.append(data.getNextTime());*/
                sb.append("&createdStartDate=");
                sb.append(taskDTO.getLastTime().minusDays(15));
                sb.append("&createdEndDate=");
                sb.append(taskDTO.getNextTime());
                sb.append("&limit=200&productInfo=true");
            } else {
                sb.append(nextCursor);
            }
            //拉取数据
            String date = walmartSdkClientService.sendWalmartGet(sb.toString(), clientId, clientSecret, walmartTokenDTO.getAccessToken(), paramMap);
            System.out.println(date);
            WalmartOrderDTO walmartOrderDTO = JSONUtil.toBean(date, WalmartOrderDTO.class);
            if (CollectionUtils.isEmpty(walmartOrderDTO.getList().getElements().getOrder())) {
                System.out.println("没有值");
                break;
            }

            nextCursor = walmartOrderDTO.getList().getMeta().getNextCursor();//下一页
            if (StringUtil.isBlank(nextCursor)) {
                break;
            }
        }

    }

    private static RedisUtil redisUtil;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil){
        this.redisUtil = redisUtil;
    }

    /**
     * 发送请求到沃尔玛获取令牌token
     * @param baseUrl 接口地址
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @return java.lang.String
     */
    public WalmartTokenDTO sendWalmartPostToken(String baseUrl, String clientId, String clientSecret) {
        String consumerId = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", WalmartStaticKey.accept_application);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.VERSION", "1.0.0");
        headers.put("Authorization", "Basic "+ Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        Map<String, Object> param = new HashMap();
        param.put("grant_type", "client_credentials");
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headers);

        WalmartTokenDTO tokenDTO = null;
        try {
            tokenDTO = JSONUtil.toBean(bodyStr, WalmartTokenDTO.class);
            log.info(String.format("::::: 沃尔玛授权 ::::: clientId => %s, clientSecret => %s, 返回参数 => %s ", clientId, clientSecret, tokenDTO));
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_AUTHORIZE_FAIL, bodyStr);
        }

        if (StringUtil.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_AUTHORIZE_FAIL, bodyStr);
        }

        return tokenDTO;
    }

    /**
     * 发送Get请求到沃尔玛
     * @param baseUrl 接口地址
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @param accessToken 短令牌
     * @param paramMap 查询参数
     * @return java.lang.String
     */
    public String sendWalmartGet(String baseUrl, String clientId,String clientSecret, String accessToken, HashMap<String, Object> paramMap) {
        Map<String, String> headers = new HashMap<String, String>();
        String consumerId = UUID.randomUUID().toString();
        headers.put("Content-Type", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("Authorization", "Basic "+Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("WM_SEC.ACCESS_TOKEN", accessToken);
        String bodyStr = OkHttpUtils.doGet(baseUrl, paramMap, headers);
        return bodyStr;
    }

    /**
     * 发送Post请求到沃尔玛
     * @param baseUrl 接口地址
     * @param clientId 账户id
     * @param clientSecret 账户秘钥
     * @param accessToken 短令牌
     * @param paramMap 查询参数
     * @return java.lang.String
     */
    public String sendWalmartPost(String baseUrl, String clientId,String clientSecret, String accessToken, HashMap<String, Object> paramMap) {
        Map<String, String> headers = new HashMap<String, String>();
        String consumerId = UUID.randomUUID().toString();
        headers.put("Content-Type", WalmartStaticKey.accept_json);
        headers.put("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME);
        headers.put("WM_QOS.CORRELATION_ID", consumerId);
        headers.put("Accept", WalmartStaticKey.accept_json);
        headers.put("Authorization", "Basic "+Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()));
        headers.put("WM_SEC.ACCESS_TOKEN", accessToken);
        String bodyStr = OkHttpUtils.doPost(baseUrl, paramMap, headers);
        return bodyStr;
    }

    /**
     * 发送POST请求
     * @Author Luo_WG
     * @Date 2023/12/19 14:30
     * @param baseUrl
     * @param clientId
     * @param clientSecret
     * @param accessToken
     * @param param
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    public Map<String, Object> sendPost(String baseUrl, String clientId,String clientSecret, String accessToken, String param) {
        Response response = null;
        Long start = System.currentTimeMillis();
        String responseString = "";
        String consumerId = UUID.randomUUID().toString();
        Map<String, Object> ressultMap = new HashMap<>();
        MediaType mediaType = MediaType.parse("application/json");
        try {
            for (int i = 0; i < 2; i++) {
                response = this.doSend(
                        new Request.Builder()
                        .header("Content-Type", WalmartStaticKey.accept_json)
                        .header("WM_SVC.NAME", WalmartStaticKey.WM_SVC_NAME)
                        .header("WM_QOS.CORRELATION_ID", consumerId)
                        .header("Accept", WalmartStaticKey.accept_json)
                        .header("Authorization", "Basic "+Base64.encodeBase64String(getclient(clientId, clientSecret).getBytes()))
                        .header("WM_SEC.ACCESS_TOKEN", accessToken)
                                .url(baseUrl)
                                .post(RequestBody.create(mediaType, param))
                                .build()
                );

                if(response.code() == 200){
                    break;
                }
            }
            responseString = response.body().string();
            ressultMap.put("code", String.valueOf(response.code()));
            ressultMap.put("msg", response.message());
            ressultMap.put("data", responseString);
            return ressultMap;
        } catch (Exception e) {
            if (e.getMessage().contains("connect timed out")) {
                ressultMap.put("code", "411");
            }else{
                ressultMap.put("code", "500");
            }
            ressultMap.put("msg", e.getMessage());
            return ressultMap;
        } finally {
            Long end = System.currentTimeMillis();
            log.info(String.format("::::: sendPost ::::: 请求地址 => %s, 请求参数 => %s, 开始时间 => %s, " +
                            "结束时间 => %s, 执行时间 => %s, 返回参数 => %s ", baseUrl, param, start, end, end - start,
                    responseString));
            if (response != null) {
                response.close();
            }
        }
    }

    public void shipOrder(WalmartShipDTO dto) {
        //  根据店铺ID获取授权
        WalmartShopInfoDTO shopInfoDTO = WalmartSdkClientService.getTokenByShopId(dto.getShopId());
        //获取令牌
        String baseUrl = WalmartStaticKey.baseUrl + "token";
        WalmartTokenDTO walmartTokenDTO = this.sendWalmartPostToken(baseUrl, shopInfoDTO.getClientId(), shopInfoDTO.getClientSecret());

        //订单发货标识
        baseUrl = WalmartStaticKey.baseUrl + "orders/{purchaseOrderId}/shipping";

        WalmartShipOrderDTO walmartShipOrderDTO = new WalmartShipOrderDTO();

        List<OrderLineBean> orderLineBeanList = new ArrayList<>();
        OrderShipmentBean orderShipmentBean = new OrderShipmentBean();

        OrderLinesBean orderLinesBean = new OrderLinesBean();

        List<WalmartShipOrderDetailDTO> detailList = dto.getDetailList();
        //详情：
        for (WalmartShipOrderDetailDTO detailDTO : detailList) {

            OrderLineBean orderLineBean = new OrderLineBean();
            //详情序号
            orderLineBean.setLineNumber(detailDTO.getPlatformLineNumber());
            //订单号：要求唯一
            orderLineBean.setSellerOrderId(dto.getSoCode());

            //订单状态
            OrderLineStatusesBean orderLineStatuses = new OrderLineStatusesBean();
            List<OrderLineStatusBean> orderLineStatus = new ArrayList<>();
            //发货状态
            orderLineStatus.get(0).setStatus("Shipped");

            //----------------设置发货数量信息-----------------------------------
            StatusQuantityBean statusQuantity = new StatusQuantityBean();
            //发货数量
            statusQuantity.setAmount(detailDTO.getQty());
            //计量单位
            statusQuantity.setUnitOfMeasurement("EACH");
            orderLineStatus.get(0).setStatusQuantity(statusQuantity);

            //----------------有关包裹装运和跟踪更新的信息列表-----------------------------------
            TrackingInfoBean trackingInfo = new TrackingInfoBean();
            //包裹的发货日期
            trackingInfo.setShipDateTime(System.currentTimeMillis());
            //有关包裹承运商的信息
            CarrierNameBean carrierName = new CarrierNameBean();
            carrierName.setCarrier(dto.getLogisticsPlatformCode());
            trackingInfo.setCarrierName(carrierName);
            //运输方式。可以是以下类型之一：Standard、Express、OneDay、WhiteGlove、Value或Freight
            trackingInfo.setMethodCode("Value");
            trackingInfo.setTrackingURL("https://www.walmart.com/tracking?tracking_id="+ dto.getTrackNo() +"");
            //获取渠道标发单号
            String trackingNumber = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(),dto.getOrderDeliveryMarkType())
                    ? dto.getTransportNo() : dto.getTrackNo();
            if (StrUtil.isBlank(trackingNumber)) {
                throw new ServiceException("操作失败，渠道标发单号为空");
            }

            trackingInfo.setTrackingNumber(trackingNumber);
            orderLineStatus.get(0).setTrackingInfo(trackingInfo);

            orderLineStatuses.setOrderLineStatus(orderLineStatus);
            orderLineBean.setOrderLineStatuses(orderLineStatuses);

            orderLineBeanList.add(orderLineBean);
        }

        orderLinesBean.setOrderLine(orderLineBeanList);

        orderShipmentBean.setOrderLines(orderLinesBean);

        walmartShipOrderDTO.setOrderShipment(orderShipmentBean);

        baseUrl = baseUrl.replace("{purchaseOrderId}", dto.getPlatformCode());

        HashMap<String, Object> map = (HashMap<String, Object>) BeanUtil.beanToMap(walmartShipOrderDTO);
        String param = JSONObject.toJSONString(map);
        Map<String, Object> resultMap = this.sendPost(baseUrl, shopInfoDTO.getClientId(), shopInfoDTO.getClientSecret(), walmartTokenDTO.getAccessToken(), param);

        if ("200".equals(resultMap.get("code"))) {
            JSONObject data = JSONObject.parseObject(resultMap.get("data").toString());
            log.info(String.format("::::: Walmart调用平台shipOrder发货订单 ::::: 请求地址 => %s, 请求参数 => %s, 开始时间 => %s, " +
                            "返回参数 => %s ", baseUrl, param, data));
        } else {
            throw new ServiceException(ApiError.WALMART_PLATFORM_SHIP_ORDER_ERROR, resultMap.get("code"));
        }
    }

    private Response doSend(Request request) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(5, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response;
    }

    /**
     * 获取Token
     */
    public static WalmartShopInfoDTO getTokenByShopId(String shopId) {
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WALMART.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof WalmartShopInfoDTO) {
                return (WalmartShopInfoDTO) tokenObj;
            }
        }
        return null;
    }


    private String getclient(String clientId, String clientSecret) {
        StringBuffer sb = new StringBuffer();
        sb.append(clientId);
        sb.append(":");
        sb.append(clientSecret);
        String str = sb.toString();
        return str;
    }

}
