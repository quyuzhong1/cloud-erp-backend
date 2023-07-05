package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductVariantDTO;
import com.erp.model.plm.dto.ProductVariantPropertyDTO;
import com.erp.model.plm.entity.ProductVariantEntity;
import com.erp.model.plm.entity.ProductVariantPropertyEntity;

import java.util.Arrays;
import java.util.List;

/**
 * @Description 产品变体值服务类
 * @Author Luo_WG
 * @Date 2022/9/26 11:19
 **/
public interface ProductVariantPropertyService extends IService<ProductVariantPropertyEntity> {
    /**
     * @Description 产品变体值查询列表
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @param variantId:产品变体类型表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantPropertyEntity>
     **/
    List<ProductVariantPropertyEntity> list(String variantId);

    /**
     * @Description 保存/修改产品变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 产品变体值信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductVariantPropertyDTO dto);

    /**
     * @Description 保存/修改产品变体类型值信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 产品变体值信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductVariantPropertyDTO> dto);

    /**
     * @Description 删除产品变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 16:13
     * @param id:产品变体值信息主键Id
     * @return java.lang.Boolean
     **/
    Boolean deleteVariant(String id);

    /**
     * 设置占用
     * @Author Luo_WG
     * @Date 2023/6/14 11:36
     * @param propertyValueList
     * @param propertyTypeList
     * @return java.lang.Boolean
     **/
    Boolean setupOccupy(List<String> propertyValueList, List<String> propertyTypeList);
}
