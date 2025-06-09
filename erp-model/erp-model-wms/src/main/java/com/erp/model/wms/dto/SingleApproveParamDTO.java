package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class SingleApproveParamDTO extends PermissionsDTO {

    /**
     * 主键id集合
     */
    @NotEmpty(message = "请选择需要审核的数据")
    private String id;

    /**
     * 类型（pass、审核通过，reject、审核不通过）
     */
    @NotBlank(message = "审核类型不能为空")
    @StateEnumValue(strValues = {"pass", "reject"}, message = "审核类型有误")
    private String type;

    /**
     * 意见
     */
    @Size(max = 255, message = "审核意见最大255个字符")
    private String comment;
}
