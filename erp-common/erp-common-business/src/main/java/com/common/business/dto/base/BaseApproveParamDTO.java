package com.common.business.dto.base;

import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/15 18:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseApproveParamDTO extends PermissionsDTO {

    /**
     * 主键id集合
     */
    @NotEmpty(message = "请选择需要审核的数据")
    private List<String> ids;

    /**
     * 类型（pass、审核通过，reject、审核不通过）
     */
    @NotBlank(message = "审核类型不能为空")
    @StateEnumValue(strValues = {"pass","reject","reject_appoint","revoke","cancel"}, message = "审核类型有误")
    private String type;

    /**
     * 意见
     */
    @Size(max = 255, message = "审核意见最大255个字符")
    private String comment;

    /**
     * 是否需要流程，false则跳过
     */
    private Boolean isNeedProcess;
    /**
     *发货日期
     */
    private LocalDate deliveryDate;
}
