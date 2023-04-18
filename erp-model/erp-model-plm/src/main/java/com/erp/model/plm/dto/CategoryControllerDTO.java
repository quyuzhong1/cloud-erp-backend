package com.erp.model.plm.dto;

import com.erp.model.plm.entity.BasicCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 产品分类DTO
 *
 * @Author Cloud
 * @Date 2023/4/18 18:28
 **/
public class CategoryControllerDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryParamDTO {
        /**
         * 分类名
         */
        private String name;

        /**
         * 分类等级
         */
        private Integer grade;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDropDownDTO {
        /**
         * 主键id
         */
        private String code;

        /**
         * 分类名
         */
        private String name;

        public CategoryDropDownDTO(BasicCategoryEntity basicCategoryEntity) {
            this.code = basicCategoryEntity.getId();
            this.name = basicCategoryEntity.getName();
        }
    }
}
