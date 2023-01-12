package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 12:02
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CfgApiFieldMapValueDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 字段映射表id
     */
    private String fieldMapId;

    /**
     * 本系统的值
     */
    @NotBlank(message = "本系统的值不能为空")
    private String selfValue;

    /**
     * 外部系统的值
     */
    @NotBlank(message = "外部系统的值不能为空")
    private String apiValue;
}
