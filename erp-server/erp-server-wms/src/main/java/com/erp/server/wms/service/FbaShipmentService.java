package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentDTO;

/**
 * <p>
 * FBI货件表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaShipmentService extends SuperService<FbaShipmentEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FbaShipmentDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaShipmentDTO.UpdateDTO dto);

    /**
     * 拉取货件信息
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    Boolean pullShipment(FbaShipmentDTO.pullShipmentDTO dto);
}
