package com.erp.server.tms.service.logistics;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.TrackNumber;
import com.sdk.tms.shopee.model.logistics.response.TrackResponse;
import com.sdk.tms.shopee.service.ShopeeShipperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        //获取商铺配置信息
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        TrackRequest trackRequest = TrackRequest.builder()
                .partnerKey(cfgAppClient.getClientSecret())
                .partnerId(Long.valueOf(cfgAppClient.getClientId()))
                .build();
        for (LogisticsQueryBaseVO logisticsQueryVO : logisticsQueryVOList) {
            LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
            ApiResult<ShopAuthEntity> shopAuth = shopeeFeign.getShopeeShopById(logisticsQueryVO.getShopeeId());
            trackRequest.setShopId(Long.valueOf(shopAuth.getData().getShopeeId()));
            trackRequest.setAccessToken(shopAuth.getData().getAccessToken());
            trackRequest.setOrderSn(logisticsQueryVO.getDeliveryNo());
            BaseResponse baseResponse = shopeeShipperService.getTrackNumber(trackRequest);
            if (Objects.nonNull(baseResponse) && Objects.nonNull(baseResponse.getResponse()) && StrUtil.isNotBlank(baseResponse.getResponse().getString("error"))) {
                TrackResponse trackResponse = JSONObject.parseObject(baseResponse.getResponse().toJSONString(), TrackResponse.class);
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.setTransportNo(trackResponse.getTrackingNumber());
                responseVO.setTrackNo(trackResponse.getTrackingNumber());
                responseVO.success();
            } else {
                isSuccess = false;
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.failure(LogisticsPlatformEnum.SHOPEE.getName(), "-1", baseResponse.getError());
            }
            responseVOS.add(responseVO);
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SHOPEE;
    }
}
