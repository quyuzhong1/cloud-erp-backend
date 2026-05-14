package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.ProductRefSkuDTO;
import com.erp.model.plm.entity.ProductRefSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品关联sku信息表 Mapper 接口
 *
 * @author codex
 * @since 2026-04-10
 */
@Mapper
public interface ProductRefSkuMapper extends BaseMapper<ProductRefSkuEntity> {

    /**
     * 根据产品id查询关联SKU明细
     *
     * @param productId 产品id
     * @return 关联SKU列表
     */
    List<ProductRefSkuDTO> listByProductId(@Param("productId") String productId);

    /**
     * 根据当前sku id查询关联SKU明细
     *
     * @param skuId 当前sku id
     * @return 关联SKU列表
     */
    List<ProductRefSkuDTO> listBySkuId(@Param("skuId") String skuId);
}
