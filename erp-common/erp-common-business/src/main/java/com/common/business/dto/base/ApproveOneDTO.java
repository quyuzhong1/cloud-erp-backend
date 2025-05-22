package com.common.business.dto.base;

import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/15 18:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveOneDTO extends PermissionsDTO {

    /**
     * 主键id集合
     */
    @NotEmpty(message = "审核id不能为空")
    private String id;

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
     * 是否是pc端访问
     */
    private Boolean pcShow = false;
    /**
     *发货日期
     */
    private LocalDate deliveryDate;

    /**
     * 流程参数map
     */
    private Map<String,Object> variablesMap;

    public ApproveOneDTO (String id,String type,String comment) {
        this.id = id;
        this.type = type;
        this.comment = comment;

    }
    public ApproveOneDTO (String id,String type,String comment,LocalDate deliveryDate) {
        this.id = id;
        this.type = type;
        this.comment = comment;
        this.deliveryDate = deliveryDate;
    }

    public ApproveOneDTO (String id,String type,String comment,Boolean isNeedProcess) {
        this.id = id;
        this.type = type;
        this.comment = comment;
        this.isNeedProcess = isNeedProcess;
    }

    /**
     * 获取审核状态
     */
    public String getApproveStatus () {
        if ("pass".equals(type)) {
            return ApproveStatusEnum.APPROVE.getStatus();
        } else if ("reject".equals(type)) {
            return ApproveStatusEnum.REJECT.getStatus();
        }  else if ("cancel".equals(type)) {
            return ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        }
        return null;
    }
}
