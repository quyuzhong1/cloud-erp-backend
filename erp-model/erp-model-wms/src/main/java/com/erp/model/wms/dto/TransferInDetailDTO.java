package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname TransferInDTO
 * @Description TODO
 * @Date 2023-05-11 11:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TransferInDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {



        /**
         * sku id
         */
        private String skuId;

        /**
         * 调入数量
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 计划调入数量
         */
        private Integer planQty;


        /**
         * 调入仓位
         */
        private String inWarehouseLocation;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

        /**
         * 途损数
         */
        private Integer transitDamageQty;

        /**
         * 途损 责任方
         */
        private String transitDamageResponsible;


        /**
         * 来源明细id
         */
        private String sourceDetailId;


    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;

        /**
         * sku id
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
         * 调入数量
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 计划调入数量
         */
        private Integer planQty;


        /**
         * 调入仓位
         */
        private String inWarehouseLocation;


        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

        /**
         * 途损数
         */
        private Integer transitDamageQty;

        /**
         * 途损 责任方
         */
        private String transitDamageResponsible;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 详情id
         */
        private String id;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 调入数量
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 计划调入数量
         */
        private Integer planQty;


        /**
         * 调入仓位
         */
        private String inWarehouseLocation;

        /**
         * 途损数
         */
        private Integer transitDamageQty;

        /**
         * 途损 责任方
         */
        private String transitDamageResponsible;


        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }
}
