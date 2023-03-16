package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 14:38
 */
@Data
@NoArgsConstructor
public class PurchaseOrderSupplierDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 采购订单id
     */
    private String purchaseOrderId;

    /**
     * 供应商id
     */
    @NotBlank(message = "供应商不能为空")
    private String supplierId;

    /**
     * 结算方式
     */
    private String settleMethod;

    /**
     * 结算币种
     */
    private String settleCurrency;

    /**
     * 供应商联系人表id
     */
    private String supplierContactId;

    /**
     * 联系人电话
     */
    private String contactPhone;
}
