package com.erp.model.scm.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
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
public class SupplierAccountDTO implements Serializable {




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
    @RegularValid(formatPattern= FieldFormatPatternTypeEnum.BANK_CARD_NO,message = "银行卡号有误")
    private String bankAccount;


    /**
     * 支行
     */
    private String bankSubbranch;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 备注
     */
    @Size(max = 255,message = "最大255字符")
    private String remark;
}
