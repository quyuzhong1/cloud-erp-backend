package com.erp.server.tms.service.logistics;

import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.tms.service.LogisticsAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
public class DsfLogisticsServiceImpl extends AbstractLogisticsService {

    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private SoInfoFeign soInfoFeign;

    @Override
    public SoInfoEntity getSoInfo(String soId) {
        return soInfoFeign.getSoInfoById(soId);
    }

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<String> createOrder(SoInfoEntity soInfo, LogisticsAddressEntity addressEntity, ProductLogisticsEntity productLogisticsEntity, LogisticsAuthEntity logisticsAuthEntity) {

        return null;
    }

    @Override
    public ApiResult<String> confirmOrder(String trackNumber, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult<String> cancelOrder(String trackNumber, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult<String> interceptOrder(String trackNumber, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult<String> updateOrder(String trackNumber, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult queryOrder(String trackNumbers, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult getLabelUrl(String trackNumber, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult getTrack(String trackNumbers, LogisticsAuthEntity logisticsAuthEntity) {
        return null;
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(LogisticsAuthEntity logisticsAuthEntity) {
        log.info("递四方获取物流渠道列表");
        return null;
    }
}
