package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;

import java.util.List;

/**
 * @Description 产品成本信息服务类
 * @Author Luo_WG
 * @Date 2022/9/22 16:15
 **/
public interface ProductCostService extends IService<ProductCostEntity> {
    /**
     * @Description 产品成本信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    List<ProductCostShowDTO> list(String productId);

    /**
    * @Description 保存/修改产品成本信息
    * @Author Luo_WG
    * @Date 2022/9/23 10:13
    * @param productCostDTO 产品成本信息表
    * @return java.lang.Boolean
    **/
    Boolean saveOrUpdate(ProductCostDTO productCostDTO);

    /**
     * @Description 保存/修改产品成本信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 18:05
     * @param productCostList 产品成本信息表
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductCostDTO> productCostList);

    /**
     * @Description 删除产品成本信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    Boolean removeCost(String skuId);
    /**
     * @description: 根据skuId查询
     * @author Will
     * @date: 2023/1/12 18:36
     * @param skuId
     * @return ProductCostEntity
     */
    ProductCostEntity getBySkuId(String skuId);
}
