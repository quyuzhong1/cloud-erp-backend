package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
    @NotBlank(message = "规划id不能为空")
    private String id;

    /**
     * 产品类型
     */
    @NotEmpty(message = "产品类型不能为空")
    private Integer type;

    /**
     * 关联产品id
     */
    private String relevanceProductId;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    private String name;

    /**
     * 产品类别ID
     */
    @NotBlank(message = "产品类别ID不能为空")
    private String categoryId;

    /**
     * 产品属性ID
     */
    @NotBlank(message = "产品属性ID不能为空")
    private String propertyId;

    /**
     * 产品负责人ID
     */
    @NotBlank(message = "产品负责人ID不能为空")
    private String chargeId;

    /**
     * 产品等级ID
     */
    @NotBlank(message = "产品等级ID不能为空")
    private String gradeId;

    /**
     * 品牌ID
     */
    @NotBlank(message = "品牌ID不能为空")
    private String brandId;

}
