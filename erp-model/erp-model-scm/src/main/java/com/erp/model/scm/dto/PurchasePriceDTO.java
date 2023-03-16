package com.erp.model.scm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname PurchasePriceDTO
 * @Description TODO
 * @Date 2023-03-16 14:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceDTO implements Serializable {


    /**
     * 表id
     */
    private String id;


    /**
     * 供应商表id
     */
    @NotBlank(message = "供应商不能为空")
    private String supplierId;

    /**
     * 报价日期
     */
    private LocalDate quotedDate;

    /**
     * 报价人id
     */
    private String pricingUserId;

    /**
     * 报价人
     */
    private String pricingUserName;

    /**
     * 采购组织
     */
    private String purchaseOrgId;

    /**
     * 采购组织名
     */
    private String purchaseOrgName;


    /**
     * 提交类型
     */
    @StateEnumValue(strValues = {"submitAudit", "create"}, message = "提交类型有误")
    private String submitType;
    /**
     * 报价明细
     */
    private List<PurchasePriceDetailDTO> purchasePriceDetailList;
}
