package com.erp.server.tms.service.logistics;

import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.server.tms.service.LogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zdy
 * @ClassName DsfLogisticsServiceImpl
 * @description: TODO
 * @date 2023年11月02日
 * @version: 1.0
 */
@Slf4j
@Component
@PlatformType(PlatformDictEnum.SDF)
public class DsfLogisticsServiceImpl implements LogisticsService {

    @Override
    public ApiResult<String> createOrder(SoInfoEntity soInfo, LogisticsAddressEntity addressEntity, ProductLogisticsEntity productLogisticsEntity, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult<String> confirmOrder(String platformCode) {
        return null;
    }

    @Override
    public ApiResult<String> cancelOrder(String platformCode) {
        return null;
    }

    @Override
    public ApiResult<String> interceptOrder(String platformCode) {
        return null;
    }

    @Override
    public ApiResult<String> updateOrder(String platformCode) {
        return null;
    }

    @Override
    public ApiResult queryOrder(String platformCode) {
        return null;
    }

    @Override
    public ApiResult getLabelUrl(String platformCode) {
        return null;
    }

    @Override
    public ApiResult getTrack(String platformCode) {
        return null;
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(String platformCode) {
        return null;
    }
}
