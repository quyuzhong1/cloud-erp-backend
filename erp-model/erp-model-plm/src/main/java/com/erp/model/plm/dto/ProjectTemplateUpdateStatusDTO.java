package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 修改状态DTO
 * @date 2022/11/16 9:36
 */
@Data
@NoArgsConstructor
public class ProjectTemplateUpdateStatusDTO implements Serializable {
    /**
     * 主键id
     */
    private String id;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "状态只能是0或者1")
    private Integer status;
}
