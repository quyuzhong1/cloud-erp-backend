package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 供应商结算信息
 * @author Lambda
 * @Classname SupplierSettlementDTO
 * @Description TODO
 * @Date 2023-03-15 17:17
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierSettlementDTO implements Serializable {


    /**
     * 结算方式
     */
    private String method;

    /**
     * 币种
     */
    private String currency;

    /**
     * 收款方
     */
    private String payee;

    /**
     * 银行名称
     */
    @Size(max = 50,message = "最大50字符")
    private String bankName;

    /**
     * 银行账号
     */
    @Size(max = 20,message = "卡号最大20字符")
    private String bankAccount;


    /**
     * 备注
     */
    @Size(max = 255,message = "最大255字符")
    private String remark;
}
