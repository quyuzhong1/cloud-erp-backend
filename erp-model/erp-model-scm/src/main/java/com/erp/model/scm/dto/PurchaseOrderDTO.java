package com.erp.model.scm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 11:16
 */
@Data
@NoArgsConstructor
public class PurchaseOrderDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 采购单号
     */
    private String code;

    /**
     * 审核状态
     */
    private String approveStatus;

    /**
     * 采购日期
     */
    private Date purchaseDate;

    /**
     * 采购员id
     */
    private String purchaseUserId;

    /**
     * 采购部门id
     */
    private String purchaseDeptId;

    /**
     * 采购组织id
     */
    private String purchaseOrgId;

    /**
     * 新品首批（false否,true是）
     */
    private Boolean isFirstMassProduct;

    /**
     * 提交类型
     */
    @StateEnumValue(strValues = {"submitAudit", "create"}, message = "提交类型有误")
    private String submitType;

    /**
     * 供应商信息
     */
    @Valid
    private PurchaseOrderSupplierDTO purchaseOrderSupplierDTO;

    /**
     * 采购订单明细
     */
    @Valid
    private List<PurchaseOrderDetailDTO> details;



    /**
     * 操作流程（仅详情显示，无需传参）
     */
    private List<PurchaseOrderProcessDTO> process;

    /**
     * 关联单据（仅详情显示，无需传参）
     */
    private PurchaseOrderRefOtherDTO  purchaseOrderRefOtherDTO;

    /**
     * 操作日志（仅详情显示，无需传参）
     */
    private List<ModuleOperateLogDTO> logs;

}
