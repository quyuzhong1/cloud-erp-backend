package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * SKU成本 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
@Mapper
public interface InventorySkuCostMapper extends BaseMapper<InventorySkuCostEntity> {
    /**
     * 分页汇总
     * @param permissionSql
     * @return
     */
    List<InventorySkuCostDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<InventorySkuCostDTO.PagingVO> paging(@Param("query") Page<InventorySkuCostDTO.PagingVO> query, @Param("params") InventorySkuCostDTO.PagingParamDTO params);

    /**
     * 导出
     * @param params
     * @return
     */
    List<InventorySkuCostDTO.PagingVO> exportList(@Param("params") InventorySkuCostDTO.PagingParamDTO params);

    /**
     * 根据组织和sku获取成本列表
     * @param orgId
     * @param skuIds
     * @param status
     * @return
     */
    List<InventorySkuCostDTO.PagingVO> listDetailByOrgIdAndSkuIds(@Param("orgId") String orgId, @Param("skuIds") List<String> skuIds, @Param("status") String status);
}
