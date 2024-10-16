package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.request.Dropoff;
import com.sdk.tms.shopee.model.logistics.request.ShipOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.tms.shopee.model.logistics.response.ShipDetailResponse;
import com.sdk.tms.shopee.model.logistics.response.ShipDropInfo;
import com.sdk.tms.shopee.model.logistics.response.TrackResponse;
import com.sdk.tms.shopee.service.ShopeeLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zdy
 * @ClassName ShopeeLogisticsHandlerImpl
 * @description: TODO
 * @date 2023年11月13日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.SHOPEE)
public class ShopeeLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private ShopeeLogisticsService shopeeLogisticsService;
    @Resource
    private ShopeeFeign shopeeFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsOperateService logisticsOperateService;


    /**
     * 虾皮  authId 需要是店铺 shopId
     *
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        //获取商铺配置信息
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        Map<String, String> map = new HashMap<>();
        map.put("id", shopId);
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("partnerKey", cfgAppClient.getClientSecret());
        map.put("partnerId", cfgAppClient.getClientId());
        map.put("host", cfgAppClient.getUrl());
        if (StringUtils.isNotBlank(shopId)) {
            ApiResult<ShopAuthEntity> shopAuth = shopeeFeign.getShopeeShopById(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getData().getShopeeId());
                map.put("token", shopAuth.getData().getAccessToken());
            }
        }
        return map;
    }

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     * 订单配货后，如果渠道是虾皮的线上物流，获取跟踪号时需要调虾皮接口ship_order，并默认选择drop_off，并调接口get_tracking_number获取跟踪号
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        Map<String, String> authMap = logisticsOrderVO.getAuthMap();
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("accessToken"))
                .host(authMap.get("host"))
                .build();
        String orderSn = logisticsOrderVO.getPlatformCode();
        String packageNumber = logisticsOrderVO.getPackageNumber();
        //标记发货
        ShipDetailResponse shippingParameter = shopeeLogisticsService.getShippingParameter(baseRequest, orderSn, packageNumber);
        ShipDropInfo dropoff = shippingParameter.getDropoff();
        ShipOrderRequest shipOrderRequest = null;
        if (CollectionUtil.isEmpty(dropoff.getBranchInfoList())){
            shipOrderRequest = ShipOrderRequest.builder()
                    .orderSn(orderSn)
                    .dropoff(Dropoff.builder().build())
                    .build();
        }else {
            String logisticsChannelName = "";
            String logisticsNo = "";
            Dropoff dropoff1 = Dropoff.builder()
                    .branchId(dropoff.getBranchInfoList().get(0).getBranchId())
                    .senderRealName(logisticsChannelName)
                    .slug(dropoff.getSlugInfoList().get(0).getSlug())
                    .trackingNumber(logisticsNo)
                    .build();
            shipOrderRequest = ShipOrderRequest.builder()
                    .orderSn(orderSn)
                    .dropoff(dropoff1)
                    .packageNumber(packageNumber)
                    .build();
        }
        BaseResponse baseResponse = shopeeLogisticsService.shippingOrder(baseRequest, shipOrderRequest);
        if (Objects.isNull(baseResponse) || StrUtil.isNotBlank(baseResponse.getError())){
            //标记发货异常
        }
        //获取跟踪号
        TrackResponse trackNumber = shopeeLogisticsService.getTrackNumber(baseRequest, orderSn);
        if (Objects.isNull(trackNumber)){
            //获取跟踪号异常

        }
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> responseVOS = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryVO : logisticsQueryVOList) {
            LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
            Map<String, String> authMap = logisticsQueryVO.getAuthMap();
            BaseRequest baseRequest = BaseRequest.builder()
                    .partnerKey(authMap.get("partnerKey"))
                    .partnerId(Long.valueOf(authMap.get("partnerId")))
                    .shopId(Long.valueOf(authMap.get("shopId")))
                    .accessToken(authMap.get("accessToken"))
                    .host(authMap.get("host"))
                    .build();
            try {
                ValidatorUtil.validateEntity(baseRequest);
                TrackResponse trackResponse = shopeeLogisticsService.getTrackNumber(baseRequest,logisticsQueryVO.getDeliveryNo());
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                if (Objects.nonNull(trackResponse) && StrUtil.isNotBlank(trackResponse.getTrackingNumber())) {
                    responseVO.setTransportNo(trackResponse.getTrackingNumber());
                    responseVO.setTrackNo(trackResponse.getTrackingNumber());
                    responseVO.success();
                    logisticsOperateService.pullOperateLog(logisticsQueryVO.getOrderId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(baseRequest), JSONUtil.toJsonStr(trackResponse));
                } else {
                    isSuccess = false;
                    responseVO.failure(LogisticsPlatformEnum.SHOPEE.getName(), logisticsQueryVO.getDeliveryNo(), "响应接口数据为空");
                    logisticsOperateService.pullOperateLog(logisticsQueryVO.getOrderId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(baseRequest), JSONUtil.toJsonStr(trackResponse));
                }
                responseVOS.add(responseVO);
            }catch (Exception e){
                isSuccess = false;
                responseVO.failure(LogisticsPlatformEnum.SHOPEE.getName(), logisticsQueryVO.getDeliveryNo(), e.getMessage());
                logisticsOperateService.pullOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(baseRequest), JSONUtil.toJsonStr(e));
                responseVOS.add(responseVO);
            }
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        ValidatorUtil.validateEntity(baseRequest);
        try {
            BaseResponse baseResponse = shopeeLogisticsService.getChannelList(baseRequest);
            if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" );
            }
            JSONObject response = baseResponse.getResponse();
            String error = response.getString("error");
            if (StrUtil.isNotEmpty(error)) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseResponse));
                log.error("获取渠道列表异常：{}", error);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" +error);
            }
            JSONArray jsonArray = response.getJSONArray("logistics_channel_list");
            //渠道列表
            List<LogisticsChannel> logisticsChannels = JSONObject.parseArray(jsonArray.toJSONString(), LogisticsChannel.class);
            //接口数据映射
            List<LogisticsSaleChannelEntity> logisticsSaleChannelEntities = LogisticsChannelConverter.INSTANCE.channelConvertByShopee(logisticsChannels);
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseResponse));
            return success(logisticsSaleChannelEntities);
        } catch (Exception e) {
            log.error("虾皮接口调用异常：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }
    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult authorization(Map<String, String> authMap){
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        ValidatorUtil.validateEntity(baseRequest);
        try {
            BaseResponse baseResponse = shopeeLogisticsService.getChannelList(baseRequest);
            if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
                return failure("授权失败");
            }
            JSONObject response = baseResponse.getResponse();
            String error = response.getString("error");
            if (StrUtil.isNotEmpty(error)) {
                return failure("授权失败");
            }
            return success("授权成功");

        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SHOPEE;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
