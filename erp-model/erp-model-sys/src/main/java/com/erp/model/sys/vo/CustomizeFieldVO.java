package com.erp.model.sys.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname CustomizeFieldVO
 * @Description TODO
 * @Date 2023-02-09 19:08
 * @Created by yl
 */
@Data
@NotBlank
public class CustomizeFieldVO  implements Serializable {

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


}
