package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname TransferInDTO

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
        @NotEmpty(message = "来源明细id不能为空")
        private String sourceDetailId;


        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

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
         * 变体信息
         */
        private String variantProperty;

        /**
         * 调出数量
         */
        private Integer qty;

        /**
         * 单位
         */
        @Deprecated
        private String unit;
        /**
         * 单位名称
         */
        private String unitName;

        /**
         * 备注
         */
        private String remark;


        /**
         * 即时库存
         */
        private Integer curInventoryQty;


        /**
         * 调出仓位
         */
        private String outWarehouseLocation;
        /**
         * 调出仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 明细id
         */
        private String id;

        /**
         * sku id
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 调出数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1,message = "数量最小值为1")
        @Max(value = 99999999,message = "数量最大值为99999999")
        private Integer qty;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大长度只能为200位")
        private String remark;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

        /**
         * 单位
         */
        private String unit;

        /**
         * 来源明细id
         */
        @NotEmpty(message = "来源明细id不能为空")
        private String sourceDetailId;

    }
}
