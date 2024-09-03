package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;

import java.util.List;

/**
 * <p>
 * fba货件装箱信息 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
public interface FbaShipmentPackingService extends SuperService<FbaShipmentPackingEntity> {

    void handle(FbaShipmentPackingDTO.PackingDTO data);

    List<FbaShipmentPackingEntity> getByMainIdAndBoxNo(String mainId, String boxNo);
}
