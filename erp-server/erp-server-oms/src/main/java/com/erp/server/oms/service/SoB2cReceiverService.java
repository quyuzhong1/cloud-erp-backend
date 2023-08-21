package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.SoB2cReceiverEntity;


/**
 * <p>
 * B2C销售订单买家信息表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cReceiverService extends SuperService<SoB2cReceiverEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:13
     * @param receiverDTO
     * @param mainId
     * @return Boolean
     */
    Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:13
     * @param receiverDTO
     * @param mainId
     * @return Boolean
     */
    Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, String mainId);
}
