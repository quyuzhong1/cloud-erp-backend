package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Classname ProductDTO
 * @Description TODO
 * @Date 2022-09-16 16:12
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductDTO implements Serializable {

    private String id;

    @NotBlank(message = "产品名不能为空")
    @Size(max = 50,message = "最大50字符")
    private String name;

    //产品分类
    @NotBlank(message = "产品分类不能为空")
    private String category;

    //产品分类id
    private String categoryId;

    //产品分属性
    @NotBlank(message = "产品属性不能为空")
    private String property;

    //产品分属性id
    private String propertyId;

    //产品分属性id
    @NotBlank(message = "产品负责人不能为空")
    private String chargeName;

    @NotBlank(message = "产品负责人id不能为空")
    private String chargeId;

    @NotBlank(message = "产品等级不能为空")
    private String  grade;

    private String  gradeId;

    @NotBlank(message = "产品品牌不能为空")
    private String  brandName;

    private String  brandId;


    @NotNull(message = "产品类型不能为空")
    @StateEnumValue(intValues = {2, 1}, message = "产品类型有误")
    private Integer type;


}
