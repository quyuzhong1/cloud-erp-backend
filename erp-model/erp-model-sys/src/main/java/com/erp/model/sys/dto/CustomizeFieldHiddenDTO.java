package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname CustomizeFieldDisplayDTO
 * @Description TODO
 * @Date 2023-02-07 9:44
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomizeFieldHiddenDTO implements Serializable {


    /**
     * 字段标题
     */
    private String fieldTitle;


    /**
     * 字段标题
     */
    private String fieldName;


    /**
     * 模块编号
     */
    private String moduleCode;


    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 是否否认
     */
    private Boolean isDefault;

    private String userId;


}
