package com.erp.model.bi.dto;/**
 * @author Lambda
 * @Classname BiCategoryDTO
 * @Description TODO
 * @Date 2023-09-19 14:41
 * @Created by yl
 */

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-19 14:41
 */
public class BiCategoryDTO {


    @Data
    @NoArgsConstructor
    public static class FirstCategoryParamsDTO extends BiFilterDTO{

        private List<String> categoryIdList;

    }

    /**
     * 产品分类信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductCategoryDTO{

        private String skuId;

        private String productName;

        private String skuNo;


        private String categoryId;

    }
}
