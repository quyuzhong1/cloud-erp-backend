package com.erp.server.oms.service;

import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;

import java.util.List;

/**
 * <p>
 * B2C销售订单表 状态服务类
 * </p>
 *
 */
public interface SoB2cStatusService{

    /**
     * 更新平台订单取消状态
     */
    Boolean updateCancelAndLog(PlatformDeliveryInterceptDTO dto);


    /**
     * 批量更新平台订单取消状态
     */
    Boolean batchUpdateCancelAndLog(List<String> soB2cIdList);

    BatchResultDTO freeze(String id, List<SoB2cEntity> soB2cEntityList);

    BatchResultDTO unfreeze(String id, List<SoB2cEntity> soB2cEntityList, List<SoB2cDetailEntity> soB2cDetailEntityList, List<SoB2cLogisticsEntity> soB2cLogisticsEntityList);
}
