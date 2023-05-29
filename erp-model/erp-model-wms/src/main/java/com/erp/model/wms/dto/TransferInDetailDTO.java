package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
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
public class TransferInDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * sku id
         */
        @NotBlank(message = "SKU 不能为空")
        private String skuId;

        /**
         * sku id
         */
        private String skuNo;

        /**
         * 调入数量
         */
        @NotNull(message ="调入数量不能为空" )
        @DecimalMin(value = "1",message = "调入数量最小值为1")
        @DecimalMax(value = "999999999",message = "调入数量最大值为999999999")
        private Integer qty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 计划调入数量
         */
        @NotNull(message ="计划调入数量不能为空" )
        @DecimalMin(value = "1",message = "计划调入数量最小值为1")
        @DecimalMax(value = "999999999",message = "计划调入数量最大值为999999999")
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
        @NotNull(message ="途损数数量不能为空" )
        @DecimalMin(value = "0",message = "途损数量最小值为0")
        @DecimalMax(value = "999999999",message = "途损数量最大值为999999999")
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
         * 变体信息
         */
        private String variantProperty;


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
         * 调出仓位 不能更改
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
    public static class UpdateDTO  extends AddDTO{

        /**
         * 详情id
         */
        private String id;


    }
}
