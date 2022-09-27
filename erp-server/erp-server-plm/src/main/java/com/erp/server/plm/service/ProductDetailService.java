package com.erp.server.plm.service;

import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;

import java.util.List;

public interface ProductDetailService {

    /**
    * @Description 产品信息查询列表
    * @Author Luo_WG
    * @Date 2022/9/22 10:28
    * @param sku:此处可能是spu，需求界面只有一个输入框可输入spuNo或者skuNo查询
    * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
    **/
    List<ProductDetailShowDTO> list(String sku);
    
    /**
    * @Description 无规格产品信息明细
    * @Author Luo_WG
    * @Date 2022/9/22 12:05
    * @param productId:产品信息表id
    * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
    **/
    ProductNoDetailDTO getNoSpecDetailById(String productId);
    
    /**
    * @Description 多规格产品信息明细
    * @Author Luo_WG
    * @Date 2022/9/22 12:05
    * @param productId:产品信息表id
    * @return java.util.List<com.erp.model.plm.dto.ProductVariantShowDTO> 
    **/
    ProductManyDetailDTO getManySpecDetailById(String productId);

    /**
     * @Description 保存/修改产品sku信息表数据
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productNoSpecDTO 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductNoSpecDTO productNoSpecDTO);

    /**
     * @Description 保存/修改产品sku信息表数据-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productDetailList 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductDetailDTO> productDetailList);

    /**
    * @Description 新增无规格sku信息
    * @Author Luo_WG
    * @Date 2022/9/21 16:31
    * @param productNoSpecDTO:新增产品无规格sku信息请求参数
    * @return java.lang.Boolean
    **/
    Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO);

    /**
    * @Description 新增多规格sku信息
    * @Author Luo_WG
    * @Date 2022/9/22 10:52
    * @param productManySpecDTO:新增产品多规格sku信息请求参数
    * @return java.lang.Boolean
    **/
    Boolean saveOrUpdateManySpec(ProductManySpecDTO productManySpecDTO);

    /**
     * @Description 多规格自动生成
     * @Author Luo_WG
     * @Date 2022/9/26 14:54
     * @param variantAutoAddDTO:自动生成请求参数
     * @return java.lang.Boolean
     **/
    List<ProductDetailEntity> InsertManySpecAuto(VariantAutoAddDTO variantAutoAddDTO);

    /**
    * @Description 删除多规格sku信息
    * @Author Luo_WG
    * @Date 2022/9/22 11:32
    * @param skuId:产品sku表主键id
    * @return java.lang.Boolean
    **/
    Boolean delete(String skuId);

    /**
     * @Description 根据产品主键id查询sku明细
     * @Author Luo_WG
     * @Date 2022/9/26 18:25
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    List<ProductDetailEntity> queryByProductId(String productId);
}
