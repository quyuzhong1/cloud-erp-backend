package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:43
 */
@Data
@NoArgsConstructor
public class ListStatusCountDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class SalesDemandCountDTO {

        /**
         * 类型(toBeApprove待审批)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;

    }

    @Data
    @NoArgsConstructor
    public static class PurchaseApplicationCountDTO {

        /**
         * 类型(toBeApprove待审批，toBeCreate待生成，created已生成，reject不通过)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;

    }

    @Data
    @NoArgsConstructor
    public static class PurchaseOrderCountDTO {

        /**
         * 类型(toBeApprove待审批，toBeCreate待到货，created已到货，reject不通过)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;

    }


    @Data
    @NoArgsConstructor
    public static class PurchaseChangeCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * SRM 订单确认列表统计
     * PurchaseOrderConfirmTypeEnum
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseOrderConfirmCountDTO {

        /**
         * 全部  all
         * 待确认  toBeConfirm
         * 已确认  confirm
         * 已拒绝  reject
         * 送货中  delivery
         * 已完成  finish
         * 已关闭  closed
         *
         */
        private String type;
        /**
         * 名称
         */
        private String name;
        /**
         * 数量
         */
        private Integer count;

    }
}
