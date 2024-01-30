package com.erp.server.wms.service;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WarehouseMappingDTO;

import java.util.List;

/**
 * <p>
 * 仓库映射第三方平台表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-30
 */
public interface WarehouseMappingService extends SuperService<WarehouseMappingEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WarehouseMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-01-30
    * @param dto
    * @return
    */
    Boolean update(WarehouseMappingDTO.UpdateDTO dto);

    /**
     * 根据仓库id查询映射信息
     * @Author Luo_WG
     * @Date 2024/1/30 18:39
     * @param warehouseIdList
     * @return java.util.List<com.erp.model.wms.dto.WarehouseMappingDTO.MappingViewDTO>
     **/
    List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByWarehouseIds(List<String> warehouseIdList);
}
