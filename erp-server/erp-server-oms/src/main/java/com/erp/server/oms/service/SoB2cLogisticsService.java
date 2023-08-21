package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;


/**
 * <p>
 * B2C销售订单物流信息表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cLogisticsService extends SuperService<SoB2cLogisticsEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:12
     * @param logisticsDTO
     * @param mainId
     * @return Boolean
     */
    Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:12
     * @param logisticsDTO
     * @param mainId
     * @return Boolean
     */
    Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId);
}
