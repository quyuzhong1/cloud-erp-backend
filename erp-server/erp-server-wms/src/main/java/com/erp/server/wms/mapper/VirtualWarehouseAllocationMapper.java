package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓分货单 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Mapper
public interface VirtualWarehouseAllocationMapper extends BaseMapper<VirtualWarehouseAllocationEntity> {

    IPage<VirtualWarehouseAllocationDTO.ListDTO> paging(Page query, @Param("params") VirtualWarehouseAllocationDTO.PagingParamDTO params);

    List<VirtualWarehouseAllocationDTO.ListDTO> listExport(@Param("params")VirtualWarehouseAllocationDTO.ExportDTO dto);
    Page<VirtualWarehouseAllocationDTO.ListDTO> listExport(@Param("page")Page<VirtualWarehouseAllocationDTO.ListDTO> page,@Param("params")VirtualWarehouseAllocationDTO.ExportDTO dto);

    Integer listCount(@Param("params")VirtualWarehouseAllocationDTO.PagingParamDTO pagingParamDTO);
    Integer listCountBySyncStatus(@Param("params")VirtualWarehouseAllocationDTO.PagingParamDTO pagingParamDTO);
}
