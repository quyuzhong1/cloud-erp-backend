package com.erp.server.wms.service;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PickingCartTypeDTO;

/**
 * <p>
 * 拣货车类型 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
public interface PickingCartTypeService extends SuperService<PickingCartTypeEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PickingCartTypeDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    Boolean update(PickingCartTypeDTO.UpdateDTO dto);


}
