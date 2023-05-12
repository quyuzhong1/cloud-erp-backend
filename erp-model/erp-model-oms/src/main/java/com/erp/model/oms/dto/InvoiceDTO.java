package com.erp.model.oms.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname InvoiceDTO
 * @Description TODO
 * @Date 2023-05-10 17:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class InvoiceDTO implements Serializable {

    /**
     * 增加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 发票抬头
         */
        @NotBlank(message = "发票抬头不能为空")
        @Size(max = 50, message = "发票抬头最大50字符")
        private String head;

        /**
         * 发票类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         *          type=InvoiceType
         *
         */
        @StateEnumValue(strValues = {"valueAddedTax","invoice"},message = "发票类型有误")
        private String type;

        /**
         * 开户银行
         */
        @NotBlank(message = "开户银行不能为空")
        @Size(max = 50, message = "开户银行最大50字符")
        private String bankName;

        /**
         * 银行账号
         */
        @Size(max = 50, message = "银行账号最大50字符")
        private String bankAccount;



        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean isDefault;


        /**
         *备注
         */
        @Size(max = 200, message = "发票备注最大200字符")
        private Boolean remark;


    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * id
         */
        private String id;

        /**
         * 发票抬头
         */
        private String head;

        /**
         * 类型
         */
        private String type;

        /**
         * 银行名称
         */
        private String bankName;

        /**
         * 银行账号
         */
        private String bankAccount;



        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean isDefault;


        /**
         *备注
         */
        private Boolean remark;
    }
}
