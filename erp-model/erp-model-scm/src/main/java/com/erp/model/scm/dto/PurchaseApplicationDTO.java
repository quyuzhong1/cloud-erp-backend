package com.erp.model.scm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 18:05
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationDTO implements Serializable {

    /**
     * 主表id
     */
    private String id;

    /**
     * 申请单号
     */
    @NotBlank(message = "申请单号不能为空")
    @Size(max = 50,message = "申请单号不能大于50字符")
    private String code;


    /**
     * 单据状态（waitSubmit待提交，auditIng审核中，auditNoPass审核不通过，finish已完成）
     */
    @NotBlank(message = "单据状态不能为空")
    private String approveStatus;

    /**
     * 申请日期
     */
    @NotEmpty(message = "申请日期不能为空")
    private LocalDate applyDate;

    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 申请人部门id
     */
    private String applyDeptId;

    /**
     * 新品首批（false否,true是）
     */
    @NotEmpty(message = "新品首批不能为空")
    private Boolean isFirstMassProduct;

    /**
     * 提交类型
     */
    @StateEnumValue(strValues = {"submitAudit", "create"}, message = "提交类型有误")
    private String submitType;

    /**
     * 采购申请明细
     */
    @Valid
    private List<PurchaseApplicationDetailDTO> details;

    /**
     * 操作日志（仅显示，无需传参）
     */
    private List<ModuleLogDTO> logs;

}
