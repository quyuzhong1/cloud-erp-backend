package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.ProductRefSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductRefSkuEntity;

import java.util.List;

/**
 * 产品关联sku信息表 服务类
 *
 * @author codex
 * @since 2026-04-10
 */
public interface ProductRefSkuService extends SuperService<ProductRefSkuEntity> {

    /**
     * 根据产品id查询关联SKU明细
     *
     * @param productId 产品id
     * @return 关联SKU列表
     */
    List<ProductRefSkuDTO> listByProductId(String productId);

    /**
     * 根据当前sku id查询关联SKU明细
     *
     * @param skuId 当前sku id
     * @return 关联SKU列表
     */
    List<ProductRefSkuDTO> listBySkuId(String skuId);

    /**
     * 单属性SKU关联SKU整页覆盖保存
     *
     * @param productId 产品id
     * @param skuId     当前sku id
     * @param dtoList   关联SKU列表
     */
    void replaceNoSpec(String productId, String skuId, List<ProductRefSkuDTO> dtoList);

    /**
     * 多属性SKU关联SKU整页覆盖保存
     *
     * @param productId       产品id
     * @param currentSkuList  当前产品下的子SKU列表
     * @param dtoList         关联SKU列表
     */
    void replaceManySpec(String productId, List<ProductDetailEntity> currentSkuList, List<ProductRefSkuDTO> dtoList);

    /**
     * 根据sku id列表删除关联SKU数据。
     * 会同时删除“所属sku”和“被关联sku”为目标sku的数据。
     *
     * @param skuIds sku id列表
     */
    void removeBySkuIds(List<String> skuIds);

    /**
     * SKU变更SPU时，批量更新所属关联SKU记录的产品id
     *
     * @param skuIds    sku id列表
     * @param productId 新产品id
     */
    void updateProductIdBySkuIds(List<String> skuIds, String productId);
}
