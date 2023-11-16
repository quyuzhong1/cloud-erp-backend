package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasInventoryDTO;

/**
 * <p>
 * 海外仓库存 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasInventoryService extends SuperService<OverseasInventoryEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasInventoryDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasInventoryDTO.UpdateDTO dto);


}
