package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.dto.ProductVariantDTO;
import com.erp.model.plm.entity.ProductVariantEntity;

import java.util.List;

/**
 * @Description 产品变体信息服务类
 * @Author Luo_WG
 * @Date 2022/9/26 11:19
 **/
public interface ProductVariantService extends IService<ProductVariantEntity> {
    /**
     * @Description 产品变体类型查询列表
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantEntity>
     **/
    List<ProductVariantEntity> list();

    /**
     * @Description 获取变体类型和变体值
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantEntity>
     **/
    List<ProductVariantEntity> listVariantAndProperty();

    /**
     * @Description 保存/修改产品变体类型信息
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param productVariantDTO 产品变体类型信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductVariantDTO productVariantDTO);

    /**
     * @Description 删除产品变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 16:13
     * @param variantId:变体类型表主键Id
     * @return java.lang.Boolean
     **/
    Boolean deleteVariant(String variantId);
}
