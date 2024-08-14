package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.entity.WarehouseLocationReplenishEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓位补货Mapper接口
 * @date 2024-06-24
 * @author tanmujin
 */
@Mapper
public interface WarehouseLocationReplenishMapper extends BaseMapper<WarehouseLocationReplenishEntity> {
    IPage<WarehouseLocationReplenishEntity> paging(@Param("page") Page<Object> page, @Param("params") WarehouseLocationReplenishDTO.SearchParamDTO params);

    List<WarehouseLocationReplenishEntity> listByParam(@Param("params") WarehouseLocationReplenishDTO.SearchParamDTO params);
    Page<WarehouseLocationReplenishEntity> listByParam(@Param("page") Page<WarehouseLocationReplenishEntity> page, @Param("params") WarehouseLocationReplenishDTO.SearchParamDTO params);

    List<WarehouseLocationReplenishDTO.TabDTO> listTabInfo();
}
