package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Will
 * @version 1.0
 * @description: API授权信息DTO
 * @date 2023/1/11 16:59
 */
@Data
@NoArgsConstructor
public class CfgApiAuthDTO {

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
     * 组别,默认default
     */
    private String group;

    /**
     * 键 (英文描述)
     */
    @NotBlank(message = "英文描述不能为空")
    private String key;

    /**
     * 授权信息（存json字符串）
     */
    @NotBlank(message = "授权信息不能为空")
    private String value;
}
