package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductVariantOptionEntity;

import java.util.List;


/**
 * 产品选择的变体类型管理
 */
public interface ProductVariantOptionService extends IService<ProductVariantOptionEntity> {
    /**
     * @Description 产品选择的变体查询
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @param productId:产品表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantPropertyEntity>
     **/
    List<ProductVariantOptionEntity> list(String productId);

    /**
     * @Description 保存/修改产品选择的变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateOptionEntity(ProductVariantOptionEntity dto);

    /**
     * @Description 保存/修改产品选择的变体类型值信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateOptionEntityBatch(List<ProductVariantOptionEntity> dto);

    /**
     * @Description 保存/修改产品选择的变体类型值信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param productId:产品表id
     * @return java.lang.Boolean
     **/
    Boolean deleteByProductId(String productId);

    /**
     * @description:
     * @author Will
     * @date: 2022/11/22 11:10
     * @param productId
     * @return List<ProductVariantOptionEntity>
     */
    List<ProductVariantOptionEntity> getByProductId(String productId);

}
