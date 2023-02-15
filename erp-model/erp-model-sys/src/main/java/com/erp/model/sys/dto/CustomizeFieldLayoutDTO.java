package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname CustomizeFieldDisplayDTO
 * @Description TODO
 * @Date 2023-02-07 9:44
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomizeFieldLayoutDTO implements Serializable {




    /**
     * 模块编号
     */
    private String moduleCode;


    /**
     * 模块名称
     */
    private String moduleName;

    /**
     *
     */
    @NotBlank(message = "字段布局不能为空")
    private String layoutJson;

    private String userId;


}
