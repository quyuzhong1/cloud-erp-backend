package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/28 17:19
 */
@Data
@NoArgsConstructor
public class BiTargetTypeDTO {

    /**
     * 指标名称
     */
    @NotNull(message = "指标名称不能为空")
    private List<String> targetNameList;

    /**
     * 指标分类
     */
    @NotBlank(message = "指标分类不能为空")
    private String targetType;
}
