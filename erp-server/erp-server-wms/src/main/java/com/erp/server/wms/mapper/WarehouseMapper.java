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
    Page<WarehouseDTO.PagingViewDTO> getExport(@Param("page") Page<WarehouseDTO.PagingViewDTO> page, @Param("params") WarehouseDTO.PagingParamDTO dto);

    IPage<WarehouseDTO.PagingNoPermissionDTO> pagingNoPermission(Page query,@Param("params")  WarehouseDTO.PagingDTO params);

    /**
     * 仓库产品列表
     * @param query
     * @param params
     * @return
     */
    IPage<WarehouseDTO.PagingProductViewDTO> pagingProduct(Page query, @Param("params") WarehouseDTO.PagingProductDTO params);

    /**
     * 根据名称获取仓库id
     * @author hyj
     * @date 2024/4/18 10:26
     * @param nameList
     */
    List<WarehouseDTO.ListDTO> getByNames(@Param("nameList") List<String> nameList);
    /**
     * @description: 根据关键词查询
     * @author Will
     * @date: 2024/5/23 19:43
     * @param params
     * @return List<WarehouseEntity>
     */
    List<WarehouseEntity> listWarehouse(@Param("params") WarehouseDTO.ListInventoryQtyParamDTO params);
    /**
     * @description: 分页远程查询
     * @author Will
     * @date: 2024/5/24 12:58
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<WarehouseDTO.ListDTO> pagingSelect(Page query,@Param("params") WarehouseDTO.SelectDTO params);
}
