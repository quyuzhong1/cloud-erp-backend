package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: API字段映射DTO
 * @date 2023/1/11 11:58
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CfgApiFieldMapDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 平台id
     */
    @NotBlank(message = "平台不能为空")
    private String apiPlatformId;

    /**
     * 模块类型 ApiModuleTypeEnum枚举,（0产品信息，1BOM管理）
     */
    @NotNull(message = "模块类型为空")
    private Integer moduleType;

    /**
     * 取值方式 ApiFieldType枚举
     */
    @NotNull(message = "取值方式为空")
    private Integer fieldType;

    /**
     * 本系统的字段
     */
    @NotBlank(message = "本系统字段不能为空")
    private String selfField;

    /**
     * 本系统的字段中文描述
     */
    private String selfFieldName;

    /**
     * 外部系统的字段（多层结构可逗号分割）
     */
    @NotBlank(message = "外部系统的字段不能为空")
    private String apiField;

    /**
     * 选项值集合
     */
    @Valid
    private List<CfgApiFieldMapValueDTO>  valueList;
}
