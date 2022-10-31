package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName product_unit
 */
@Data
public class ProductUnitDTO implements Serializable {

    /**
     * 主键id 无id：修改 有id：新增
     */
    private String id;

    /**
     * 单位名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}