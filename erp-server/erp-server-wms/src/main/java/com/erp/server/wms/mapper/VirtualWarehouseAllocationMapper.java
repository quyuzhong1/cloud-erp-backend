package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


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
}
