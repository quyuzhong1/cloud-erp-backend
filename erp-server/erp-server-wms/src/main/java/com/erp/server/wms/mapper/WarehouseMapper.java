package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 仓库表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<WarehouseEntity> {

    IPage<WarehouseDTO.PagingViewDTO> paging(Page query, @Param("params") WarehouseDTO.PagingParamDTO params);

    List<WarehouseDTO.PagingViewDTO> getExport(@Param("params") WarehouseDTO.PagingParamDTO dto);
}
