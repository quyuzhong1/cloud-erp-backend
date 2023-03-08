package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 模板新增或修改DTO
 * @date 2022/11/16 9:30
 */
@Data
@NoArgsConstructor
public class ProjectTemplateSaveOrUpdateDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;



    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 50,message = "最大50字符")
    private String name;

    /**
     * 产品属性id
     */
    @NotNull(message = "产品属性不能为空")
    @Size(min = 1,message = "至少需要选择一个产品属性")
    private List<String> productPropertyIdList;
}
