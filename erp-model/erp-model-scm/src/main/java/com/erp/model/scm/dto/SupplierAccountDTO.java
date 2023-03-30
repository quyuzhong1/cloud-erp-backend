package com.erp.model.scm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 供应商账户信息
 *
 * @author Lambda
 * @Classname SupplierAccountDTO
 * @Description TODO
 * @Date 2023-03-17 14:51
 * @Created by yl
 */
public class SupplierAccountDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {


        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String supplierName;

        /**
         * 收款方
         */
        @Size(max = 50, message = "账户名称最大100字符")
        private String payee;


        //@NotBlank(message = "银行不能为空")
        private String bankId;


        /**
         * 银行账号
         */
        @Size(max = 20, message = "卡号最大20字符")
        // @RegularValid(formatPattern= FieldFormatPatternTypeEnum.BANK_CARD_NO,message = "银行卡号有误")
        private String bankAccount;


        /**
         * 支行
         */
        @Size(max = 255, message = "账户名称最大255字符")
        private String bankSubbranch;

        /**
         * 支付方式
         */
        // @NotBlank(message = "支付方式不能为空")
        private String payMethodId;

        /**
         * 备注
         */
        @Size(max = 255, message = "账户信息备注最大255字符")
        private String remark;

    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends SupplierAccountDTO.AddDTO {
        /**
         * 主表id
         */
        private String id;
    }
}
