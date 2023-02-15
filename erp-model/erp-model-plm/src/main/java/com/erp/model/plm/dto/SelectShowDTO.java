package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: 下拉框返回DTO
 * @date 2022/11/24 11:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectShowDTO {

    /**
     * 下拉框值
     */
    private Integer value;

    /**
     * 下拉框标签名称
     */
    private String label;

    /**
     * 下拉框描述
     */
    private String desc;
}
