package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/10 14:11
 */
@Data
@NoArgsConstructor
public class TransferApplicationDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1,message = "数量最小值为1")
        @Max(value = 999999999,message = "数量最大值为999999999")
        private Integer qty;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

        /**
         * 是否自动生成加工单
         */
        private Boolean isAutoMachine;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 商品状态
         */
        private String saleStateName;

        /**
         * 单位
         */
        private String unit;

        /**
         * 及时库存
         */
        private Integer curInventoryQty;

    }

    @Data
    @NoArgsConstructor
    public class ApproveDTO extends AddDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 主数据ID
         */
        private String mainId;

        /**
         * 入库仓库名称
         */
        private String inWarehouseName;

        /**
         * 入库仓管员ID
         */
        private String inWarehouseChargeId;

        /**
         * 出库仓库名称
         */
        private String outWarehouseName;

        /**
         * 出库仓管员ID
         */
        private String outWarehouseChargeId;
    }
}
