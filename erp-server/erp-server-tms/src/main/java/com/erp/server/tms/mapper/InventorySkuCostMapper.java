package com.erp.server.tms.mapper;

import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * SKU存货成本 Mapper 接口
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
}
