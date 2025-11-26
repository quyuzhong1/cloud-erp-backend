package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 产品品牌DTO
 * @TableName product_brand
 */
@Data
public class ProductBrandDTO implements Serializable {

    /**
     * 主键id 无id：修改 有id：新增
     */
    private String id;

    /**
     * 品牌名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}

