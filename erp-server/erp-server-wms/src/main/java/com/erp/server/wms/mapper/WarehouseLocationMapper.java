package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 仓库仓位表 Mapper 接口
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@Mapper
public interface WarehouseLocationMapper extends BaseMapper<WarehouseLocationEntity> {

    IPage<WarehouseLocationDTO.LocationSelectDTO> paging(Page query, @Param("params") WarehouseLocationDTO.WarehouseLocationSearchParamDTO dto);

    List<WarehouseLocationEntity> list(@Param(value = "warehouseIds") List<String> warehouseIds);
}
