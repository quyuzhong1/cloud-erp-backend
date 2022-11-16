package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板修改状态DTO
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
     * 模板名称
     */
    @NotNull(message = "模板状态不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "模板状态只能是0或者1")
    private Integer status;
}
