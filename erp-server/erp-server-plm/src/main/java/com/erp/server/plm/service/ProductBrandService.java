package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductBrandDTO;
import com.erp.model.plm.entity.ProductBrandEntity;

import java.util.List;

/**
 * 产品品牌服务类
 * @Author Auto
 * @Date 2025/01/20
 **/
public interface ProductBrandService extends IService<ProductBrandEntity> {

    /**
     * 保存/修改产品品牌-批量
     * @param productBrandList 产品品牌新增信息
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductBrandDTO> productBrandList);

    /**
     * 查询产品品牌
     * @return java.util.List<com.erp.model.plm.entity.ProductBrandEntity>
     **/
    List<ProductBrandEntity> listProductBrand();

    /**
     * 删除产品品牌
     * @param id 主键id
     * @return java.lang.Boolean
     **/
    Boolean delete(String id);

    /**
     * 查询品牌名称是否存在
     * @param name 品牌名称
     * @return ProductBrandEntity
     **/
    ProductBrandEntity checkBrandName(String name);

    /**
     * 设置占用
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean setupOccupy(List<String> ids);

    /**
     * 根据品牌名称查询
     * @param name
     * @return ProductBrandEntity
     */
    ProductBrandEntity getByName(String name);
}

