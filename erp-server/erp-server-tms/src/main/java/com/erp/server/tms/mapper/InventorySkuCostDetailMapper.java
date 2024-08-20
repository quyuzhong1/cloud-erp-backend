package com.erp.server.tms.mapper;

import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * SKU成本明细 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
@Mapper
public interface InventorySkuCostDetailMapper extends BaseMapper<InventorySkuCostDetailEntity> {
    /**
     * 根据sku查询明细
     * @param skuIds
     * @return
     */
    List<InventorySkuCostDTO.PagingVO> listDetailBySkuIds(@Param("skuIds") List<String> skuIds);
}
