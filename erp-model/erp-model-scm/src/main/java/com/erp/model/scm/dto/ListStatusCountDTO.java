package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
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
}
