package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态统计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabListDTO {

    /**
     * 类型
     */
    private String tabFlag;

    /**
     * 类型名称
     */
    private String tabFlagName;

    /**
     * 数量
     */
    private Integer count;
}