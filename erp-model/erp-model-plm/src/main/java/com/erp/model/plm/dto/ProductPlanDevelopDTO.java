package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:45
 */
@Data
@NoArgsConstructor
public class ProductPlanDevelopDTO implements Serializable {

    /**
     * 规划id
     */
    private String id;

    /**
     * 产品类型
     */
    private Integer type;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品类别ID
     */
    private String categoryId;

    /**
     * 产品属性ID
     */
    private String propertyId;

    /**
     * 产品负责人ID
     */
    private String chargeId;

    /**
     * 产品等级ID
     */
    private String gradeId;

    /**
     * 品牌ID
     */
    private String brandId;

}
