package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
public class TransferOutDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * sku id
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * 调出数量
         */
        @NotNull(message = "调出数量不能为空")
        @Min(value = 1, message = "调出数量不能小于1")
        @Max(value = 999999999,message = "出数量最大值为999999999")
        private Integer qty;

        /**
         * 备注
         */
        private String remark;


        /**
         * 来源明细id
         */
        private String sourceDetailId;


        /**
         * 调出仓位
         */
        private String warehouseLocation;

        /**
         * 单位
         */
        private String unit;


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
         * 调出数量
         */
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 备注
         */
        private String remark;


        /**
         * 即时库存
         */
        private Integer curInventoryQty;


        /**
         * 来源明细id
         */
        private String sourceDetailId;


        /**
         * 调出仓位
         */
        private String warehouseLocation;

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
         * 调出数量
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;


        /**
         * 来源明细id
         */
        private String sourceDetailId;


        /**
         * 调出仓位
         */
        private String warehouseLocation;

        /**
         * 单位
         */
        private String unit;
    }
}
