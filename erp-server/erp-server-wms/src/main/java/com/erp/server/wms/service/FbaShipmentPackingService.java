package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;

/**
 * <p>
 * fba货件装箱信息 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
public interface FbaShipmentPackingService extends SuperService<FbaShipmentPackingEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-09-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FbaShipmentPackingDTO.AddDTO dto);


    void handle(FbaShipmentPackingEntity data);
}
