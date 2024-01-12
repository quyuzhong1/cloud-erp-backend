package com.erp.model.srm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 首页数据DTO
 * </p>
 *
*/
@Data
@NoArgsConstructor
public class HomePageDTO implements Serializable {

    /**
    * 用户信息
    */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountInfoDTO {
        /**
         * 公司名称
         */
        private String companyName;

        /**
         * 公司状态
         */
        private String companyStatus;

        /**
         * 姓名
         */
        private String userName;

        /**
         * 电话
         */
        private String phone;

        /**
         * 微信昵称
         */
        private String wxName;

    }


    /**
     * 用户信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToDoItems {
        /**
         * 待确认订单数
         */
        private Integer waitConfirmOrderCount;

        /**
         * 待打印送货单数
         */
        private Integer waitPrintDeliveryCount;

        /**
         * 待确认送货单数
         */
        private Integer waitConfirmDeliveryCount;

        /**
         * 待确认对账单数
         */
        private Integer waitConfirmReconciliationCount;
    }
}