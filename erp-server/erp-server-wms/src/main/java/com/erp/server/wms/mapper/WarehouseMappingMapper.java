package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 仓库映射第三方平台表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-30
 */
@Mapper
public interface WarehouseMappingMapper extends BaseMapper<WarehouseMappingEntity> {

    /**
     * 根据仓库id查询映射信息
     * @Author Luo_WG
     * @Date 2024/1/31 9:02
     * @param warehouseIdList
     * @return java.util.List<com.erp.model.wms.dto.WarehouseMappingDTO.MappingViewDTO>
     **/
    List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByWarehouseIds(@Param("warehouseIdList") List<String> warehouseIdList);
}
