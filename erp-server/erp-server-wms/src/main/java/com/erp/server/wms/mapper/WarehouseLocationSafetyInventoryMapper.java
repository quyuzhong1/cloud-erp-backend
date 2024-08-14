package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDTO;
import com.erp.model.wms.entity.WarehouseLocationSafetyInventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓位安全库存Mapper接口
 * @date 2024-06-21
 * @author tanmujin
 */
@Mapper
public interface WarehouseLocationSafetyInventoryMapper extends BaseMapper<WarehouseLocationSafetyInventoryEntity> {
    IPage<WarehouseLocationSafetyInventoryDTO.ViewDTO> paging(Page<Object> page, @Param("param") WarehouseLocationSafetyInventoryDTO.SearchParamDTO searchParamDto);

    List<WarehouseLocationSafetyInventoryEntity> listByParam(@Param("param") WarehouseLocationSafetyInventoryDTO.SearchParamDTO searchParam);

    Page<WarehouseLocationSafetyInventoryEntity> listByParam(@Param("page") Page<WarehouseLocationSafetyInventoryEntity> page,@Param("param") WarehouseLocationSafetyInventoryDTO.exportParamDTO params);
}
