package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jim
 * @ClassName AmazonLogisticsHandlerImpl
 * @description: 亚马逊物流接口开发
 * @date 2023年12月25日
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.AMZ_MULTI_CHANNEL)
public class AmzMultiChannelLogisticsHandlerImpl extends AbstractLogisticsHandler {
    /**
     * 渠道查询
     *
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        List<LogisticsSaleChannelEntity> entityList = new ArrayList<>();
      return success(entityList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.AMZ_MULTI_CHANNEL;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
    /**
     * 授权判断
     *
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return success("授权成功");
    }
}
