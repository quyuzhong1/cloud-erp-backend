package com.erp.server.wms.service;

import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.FbaShipmentStatusEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentStatusDTO;

import java.util.List;

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

    /**
     * 根据主表id查询货件状态信息
     * @Author Luo_WG
     * @Date 2023/11/15 16:29
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.FbaShipmentStatusEntity>
     **/
    List<FbaShipmentStatusEntity> listByMainIds(List<String> mainIds);
}
