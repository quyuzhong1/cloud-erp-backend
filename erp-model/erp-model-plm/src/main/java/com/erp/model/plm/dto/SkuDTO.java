package com.erp.model.plm.dto;/**
 * @author Lambda
 * @Classname SkuDTO
 * @Description TODO
 * @Date 2023-09-19 11:33
 * @Created by yl
 */

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-19 11:33
 */
public class SkuDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class SalesDTO {

        /**
         * sku Id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 公司首单日期
         */
        private LocalDate firstOrderDate;

        /**
         * 销售状态
         */
        private Integer saleState;

        /**
         * 销售状态名
         */
        private String saleStateName;


    }
}
