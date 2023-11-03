package com.erp.server.wms.service;

import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentStatusEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentStatusDTO;

/**
 * <p>
 * FBA货件状态信息 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
public interface FbaShipmentStatusService extends SuperService<FbaShipmentStatusEntity> {


    /**
     * 通过FbaShipmentEntity保存货件状态信息
     * @author  Jim
     * @date 2023/11/2
     */
    void saveByFbaShipment(FbaShipmentEntity entity);
}
