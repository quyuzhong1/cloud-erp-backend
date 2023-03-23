package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 14:43
 */
@Data
@NoArgsConstructor
public class PurchaseOrderDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        @NotBlank(message = "sku编码不能为空")
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关名称
         */
        private String declareName;

        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 16,fraction = 4,message = "含税单价最大16字符，小数位不能大于4个字符")
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 16,fraction = 4,message = "税率最大16字符，小数位不能大于4个字符")
        private BigDecimal taxRate;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @Min(value = 0,message = "采购数量最小值为0")
        @Max(value = 99999999,message = "采购数量最大值为99999999")
        private Integer purchaseQty;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount;

        /**
         * 预计交货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 收料组织id
         */
        @NotBlank(message = "收料组织不能为空")
        private String receiveOrgId;

        /**
         * 交货仓库id
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseId;

        /**
         * 是否是赠品（false否，true是）
         */
        @NotNull(message = "是否是赠品不能为空")
        private Boolean isGift;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

        /**
         * 采购申请明细id(无需传值，后端使用)
         */
        private String purchaseApplicationDetailId;

        /**
         * 采购申请id(无需传值，后端使用)
         */
        private String purchaseApplicationId;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO{

        /**
         * 主表id
         */
        private String id;
    }

}
