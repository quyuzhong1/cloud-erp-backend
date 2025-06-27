package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Classname BasicCategoryDTO

 * @Date 2022-09-13 14:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasicCategoryDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 分类名
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 父 级id
     */
    private String pid;

    /**
     * 产品数量
     */
    private Long productQuantity;

    /**
     * 分类集合
     */
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private List<BasicCategoryDTO> childrenList;




    @Data
    @NoArgsConstructor
    public static class DropdownDTO {

        private String id;

        /**
         */
        private String pid;

        /**
         */
        private String fullName;

    }

}
