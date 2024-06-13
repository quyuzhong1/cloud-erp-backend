package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Mapper
public interface VirtualWarehouseMapper extends BaseMapper<VirtualWarehouseEntity> {

    IPage<VirtualWarehouseDTO.ListDTO> paging(Page query, @Param("params") VirtualWarehouseDTO.PagingParamDTO params);

    List<VirtualWarehouseDTO.VwDTO> getByNames(@Param("nameList")List<String> nameList);
}
