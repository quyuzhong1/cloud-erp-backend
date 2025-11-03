package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 类型和值参数 DTO
 * @author wuhaotian
 * @since 2025-09-24
 */
@Data
@NoArgsConstructor
public class TypeAndValueDTO {

    /**
     * 类型参数
     */
    private String type;

    /**
     * 值参数
     */
    private String value;
}
