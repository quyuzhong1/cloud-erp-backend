package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname CategoryModuleDTO
 * @Description TODO
 * @Date 2022-12-12 18:26
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CategoryModuleDTO implements Serializable {

    /**
     * 分类id
     */
    private String categoryId;


    /**
     * 分类名
     */
    private String categoryName;


    private List<ModuleDTO> moduleList;
}
