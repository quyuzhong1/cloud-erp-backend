package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/28 14:18
 */
@Data
@NoArgsConstructor
public class BiDataSourceCustomTableDTO {

    /**
     * 模块id
     */
    @NotBlank(message = "模块id不能为空")
    private String moduleId;

    /**
     * 年份
     */
    @NotNull(message = "年份不能为空")
    private Integer year;

    /**
     * 类型(1年，2季度，3月，4周，5日）
     */
    @NotNull(message = "类型不能为空")
    private Integer type;

    /**
     * 指标类型
     */
    private String targetType;
}
