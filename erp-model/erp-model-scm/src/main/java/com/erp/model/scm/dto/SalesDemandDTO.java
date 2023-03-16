package com.erp.model.scm.dto;

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
 * @date 2023/3/15 17:23
 */
@Data
@NoArgsConstructor
public class SalesDemandDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 备货编号
     */
    @NotBlank(message = "备货编号不能为空")
    @Size(max = 50,message = "备货编号不能大于50字符")
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
     * 店铺id
     */
    private String shopId;

    /**
     * 备货原因
     */
    @Size(max = 255,message = "备货原因不能大于255字符")
    private String remark;

    /**
     * 产品信息
     */
    @Valid
    private List<SalesDemandDetailDTO> details;

    /**
     * 操作日志（仅显示，无需传参）
     */
    private List<ModuleLogDTO> logs;
}
