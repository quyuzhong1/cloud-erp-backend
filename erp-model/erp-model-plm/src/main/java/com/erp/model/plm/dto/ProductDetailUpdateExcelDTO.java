package com.erp.model.plm.dto;

import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品sku信息更新
 * Luo_WG
 * 2022/9/27 16:23
 **/
@Data
@NoArgsConstructor
public class ProductDetailUpdateExcelDTO {

    /**
     * sku编号
     */
    @FieldValid(fieldName = "sku编号", isNotBlank = true)
    private String skuNo;

    /**
     * 一级分类
     */
    @FieldValid(fieldName = "一级分类")
    private String mainCategory;

    /**
     * 二级分类
     */
    @FieldValid(fieldName = "二级分类")
    private String secondaryCategory;

    /**
     * 应用分类名
     */
    @FieldValid(fieldName = "应用分类")
    private String applicationCategoryName;

    /**
     * 品牌
     */
    @FieldValid(fieldName = "品牌", maxLength = 30)
    private String brandName;

    /**
     * 研发团队
     */
    @FieldValid(fieldName = "研发团队", maxLength = 50)
    private String rdtTeamName;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 产品类别id
     */
    private String categoryId;

    /**
     * 应用分类id
     */
    private String applicationCategoryId;

}
