package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentReceiveDTO;

import java.util.List;

/**
 * <p>
 * FBA货件签收信息 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
public interface FbaShipmentReceiveService extends SuperService<FbaShipmentReceiveEntity> {

    /**
     * 根据详情id查询收货记录
     * @param detailIds
     * @return java.util.List<com.erp.model.wms.entity.FbaShipmentReceiveEntity>
     **/
    List<FbaShipmentReceiveEntity> listByDetailIds(List<String> detailIds);
}
