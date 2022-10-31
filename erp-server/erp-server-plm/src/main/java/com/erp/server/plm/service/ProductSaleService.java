package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.entity.ProductSaleEntity;

import java.util.List;

/**
 * @Description 产品销售信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 14:04
 **/
public interface ProductSaleService extends IService<ProductSaleEntity> {
    /**
     * @Description 产品销售信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductSaleShowDTO>
     **/
    List<ProductSaleShowDTO> list(String productId);

    /**
     * @Description 保存/修改产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSaleDTO 产品销售信息表请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductSaleDTO productSaleDTO);

    /**
     * @Description 保存/修改产品销售信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:11
     * @param productSaleList 产品销售信息表请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductSaleDTO> productSaleList);

    /**
     * @Description 删除产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    Boolean removeSale(String skuId);
}
