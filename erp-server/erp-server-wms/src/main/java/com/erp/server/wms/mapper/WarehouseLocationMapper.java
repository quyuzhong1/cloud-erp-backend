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
    /**
     * @description: 根据仓库id和库位集合查询
     * @author Will
     * @date: 2023/8/2 17:52
     * @param listParam
     * @return List<WarehouseLocationEntity>
     */
    List<WarehouseLocationEntity> listByWarehouseIdAndCode(@Param("listParam") List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> listParam);

    /**
     * 根据仓库id和仓位编码获取
     * @param query
     * @param params
     * @return
     */
    Page<WarehouseLocationDTO.PagingViewDTO> pagingByParams(Page<WarehouseLocationDTO.PagingViewDTO> query,@Param("params") WarehouseLocationDTO.PagingParamDTO params);
    /**
     * @description: 分页远程查询下拉
     * @author Will
     * @date: 2024/5/16 15:20
     * @param query
     * @param params
     * @return IPage<LocationListDTO>
     */
    IPage<WarehouseLocationDTO.LocationListDTO> pagingSelect(Page query,@Param("params") WarehouseLocationDTO.SelectDTO params);
}
