package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
    private String name;
}
