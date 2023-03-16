package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
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
 * @date 2023/3/16 12:04
 */
@Data
@NoArgsConstructor
public class PurchaseChangeDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 单据编号
     */
    private String code;

    /**
     * 采购订单id
     */
    private String purchaseOrderId;

    /**
     * 审核状态
     */
    private String approveStatus;

    /**
     * 变更日期
     */
    private Date changeDate;

    /**
     * 变更人id
     */
    private String changeUserId;

    /**
     * 变更部门id
     */
    private String changeDeptId;

    /**
     * 采购组织id
     */
    private String purchaseOrgId;

    /**
     * 新品首批（false否,true是）
     */
    private Boolean isFirstMassProduct;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 提交类型
     */
    @StateEnumValue(strValues = {"submitAudit", "create"}, message = "提交类型有误")
    private String submitType;

    /**
     * 变更明细
     */
    @Valid
    private List<PurchaseChangeDetailDTO> details;
}
