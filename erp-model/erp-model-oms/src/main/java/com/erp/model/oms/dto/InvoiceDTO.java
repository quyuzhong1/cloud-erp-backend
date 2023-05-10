package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
