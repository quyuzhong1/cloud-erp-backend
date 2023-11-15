package com.erp.server.tms.service.impl;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.LogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName LogisticsBaseServiceImpl
 * @description: TODO
 * @date 2023年11月15日
 * @version: 1.0
 */
@Slf4j
@Service
public class LogisticsBaseServiceImpl implements LogisticsBaseService {
    @Resource
    private ShopeeFeign shopeeFeign;
    @Resource
    private LogisticsRegistry logisticsRegistry;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Override
    public ApiResult syncLogisticsChannel(String platform) {
        if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platform)) {
            return syncShoppeeChannel(platform);
        } else {
            return syncSingleChannel(platform);
        }
    }

    @Override
    public ApiResult syncAllLogisticsChannel() {
        log.info("====全部渠道同步开始=====");
        LogisticsPlatformEnum[] platformEnums = LogisticsPlatformEnum.values();
        for (LogisticsPlatformEnum platformEnum : platformEnums) {
            if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platformEnum.getCode())) {
                syncShoppeeChannel(platformEnum.getCode());
            } else {
                syncSingleChannel(platformEnum.getCode());
            }
        }
        log.info("=====渠道同步结束=====");
        return ApiResult.success();
    }

    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        LogisticsService service = logisticsRegistry.getHandler(LogisticsPlatformEnum.SHOPEE.getCode());
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = service.queryOrderList(logisticsQueryVOList);
        return listApiResult;
    }

    private ApiResult syncSingleChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        String authId = "";
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        LogisticsService service = logisticsRegistry.getHandler(platform);
        Map<String, String> map = service.getLogisticsAuthConfig(authId);
        if (Objects.isNull(map)) return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR);
        chanelQueryVO.setAuthMap(map);
        ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
        //把结果存储数据库
        if (channels.isSuccess()) {
            channels.getData().forEach(logisticsSaleChannelEntity -> {
                logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
            });

        } else {
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, channels.getMsg());
        }
        log.info("{}渠道同步结束", platform);
        return ApiResult.success();
    }

    private ApiResult syncShoppeeChannel(String platform) {
        log.info("{}渠道同步开始", platform);
        ApiResult<List<ShopAuthEntity>> result = shopeeFeign.getShopeeShopList("shopee_shop", "already");
        if (result.isSuccess()) {
            LogisticsService service = logisticsRegistry.getHandler(platform);
            result.getData().forEach(shopAuthEntity -> {
                ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
                Map<String, String> map = service.getLogisticsAuthConfig(shopAuthEntity.getShopId());
                chanelQueryVO.setAuthMap(map);
                ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
                if (channels.isSuccess()) {
                    channels.getData().forEach(logisticsSaleChannelEntity -> {
                        logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
                    });

                } else {
                    log.error(channels.getMsg());
                }
            });
        }
        log.info("{}渠道同步结束", platform);
        return ApiResult.success();
    }
}
