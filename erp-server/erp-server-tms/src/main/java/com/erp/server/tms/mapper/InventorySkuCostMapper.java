package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
     *
     * @param orgId
     * @param skuIds
     * @param status
     * @param month
     * @return
     */
    List<InventorySkuCostDTO.PagingVO> listDetailByOrgIdAndSkuIds(@Param("orgId") String orgId, @Param("skuIds") List<String> skuIds, @Param("status") String status, @Param("month")LocalDate month, @Param("warehouseId") String warehouseId);

    /**
     * 获取sku成本
     * @param skuIds
     * @param warehouseIds
     * @param salesOrgIds
     * @return
     */
    List<InventorySkuCostDTO.SkuCostDTO> listSkuCost(@Param("skuIds") List<String> skuIds, @Param("warehouseIds") List<String> warehouseIds, @Param("salesOrgIds") List<String> salesOrgIds,@Param("month")String month);

    /**
     * 根据skuId和仓库Id、orgId查询最新已审核单据的SKU成本（人民币）
     * @param skuIds
     * @param warehouseIds
     * @param orgId
     * @return
     */
    List<InventorySkuCostDTO.SkuCostCNYDTO> getSkuCostInCNY(@Param("skuIds") List<String> skuIds, @Param("warehouseIds") List<String> warehouseIds, @Param("orgId") String orgId);
}
