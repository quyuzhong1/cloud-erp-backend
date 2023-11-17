package com.erp.server.tms.service.logistics;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.tms.shopee.model.logistics.response.TrackResponse;
import com.sdk.tms.shopee.service.ShopeeShipperService;
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
    private ShopeeShipperService shopeeShipperService;
    @Resource
    private ShopeeFeign shopeeFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;


    /**
     * 虾皮  authId 需要是店铺 shopId
     * @param authId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        if (StringUtils.isBlank(authId)) return null;
        ApiResult<ShopAuthEntity> shopAuth = shopeeFeign.getShopeeShopById(authId);
        if (Objects.isNull(shopAuth)) return null;
        //获取商铺配置信息
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        Map<String, String> map = new HashMap<>();
        map.put("id", authId);
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("partnerKey",cfgAppClient.getClientSecret());
        map.put("partnerId",cfgAppClient.getClientId());
        map.put("shopId",shopAuth.getData().getShopeeId());
        map.put("accessToken",shopAuth.getData().getAccessToken());
        return map;
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
            TrackRequest trackRequest = TrackRequest.builder()
                    .partnerKey(authMap.get("partnerKey"))
                    .partnerId(Long.valueOf(authMap.get("partnerId")))
                    .shopId(Long.valueOf(authMap.get("shopId")))
                    .accessToken(authMap.get("accessToken"))
                    .orderSn(logisticsQueryVO.getDeliveryNo())
                    .build();
            BaseResponse baseResponse = shopeeShipperService.getTrackNumber(trackRequest);
            if (Objects.nonNull(baseResponse) && Objects.nonNull(baseResponse.getResponse()) && StrUtil.isNotBlank(baseResponse.getResponse().getString("error"))) {
                TrackResponse trackResponse = JSONObject.parseObject(baseResponse.getResponse().toJSONString(), TrackResponse.class);
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.setTransportNo(trackResponse.getTrackingNumber());
                responseVO.setTrackNo(trackResponse.getTrackingNumber());
                responseVO.success();
                logisticsOrderOperateLogService.addOperateLog(authMap.get("id"),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(trackRequest), JSONUtil.toJsonStr(baseResponse));
            } else {
                isSuccess = false;
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.failure(LogisticsPlatformEnum.SHOPEE.getName(), "-1", baseResponse.getError());
                logisticsOrderOperateLogService.addOperateLog(authMap.get("id"),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(trackRequest), JSONUtil.toJsonStr(baseResponse));
            }
            responseVOS.add(responseVO);
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
                .accessToken(authMap.get("accessToken"))
                .build();
        BaseResponse baseResponse = shopeeShipperService.getChannelList(baseRequest);
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseResponse));
            return failure();
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseResponse));
            log.error("获取渠道列表异常：{}", error);
            return failure();
        }
        JSONArray jsonArray = (JSONArray) response.get("logistics_channel_list");
        //渠道列表
        List<LogisticsChannel> logisticsChannels = JSONObject.parseArray(jsonArray.toJSONString(), LogisticsChannel.class);
        //接口数据映射
        List<LogisticsSaleChannelEntity> logisticsSaleChannelEntities = LogisticsChannelConverter.INSTANCE.channelConvertByShopee(logisticsChannels);
        logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseResponse));
        return success(logisticsSaleChannelEntities);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SHOPEE;
    }
}
