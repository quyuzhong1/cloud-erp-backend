package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname MoveCategoryDTO
 * @Description TODO
 * @Date 2022-09-17 11:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MoveCategoryDTO implements Serializable {

    /**
     * 产品id 集合
     */
    @NotNull(message = "产品id集合不能为空")
    private List<String> productIds;

    /**
     * 分类id
     */
    @NotBlank(message = "分类id 不能为空")
    private String  categoryId;
}
