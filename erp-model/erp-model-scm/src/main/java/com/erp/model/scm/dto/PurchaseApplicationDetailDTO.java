package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/16 10:34
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 采购申请id
         */
        private String purchaseApplicationId;

        /**
         * 来源单据详情id
         */
        private String sourceDetailId;

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * sku编码
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
         * 单箱数量
         */
        @Min(value = 0,message = "计划备货数量最小值为0")
        @Max(value = 999999999,message = "计划备货数量最大值为999999999")
        private Integer unitQty;

        /**
         * 最小起订量
         */
        @Min(value = 0,message = "最小起订量最小值为0")
        @Max(value = 999999999,message = "最小起订量最大值为999999999")
        private Integer moq;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 申请数量
         */
        @NotNull(message = "申请数量不能为空")
        @Min(value = 1,message = "申请数量最小值为1")
        @Max(value = 999999999,message = "申请数量最大值为999999999")
        private Integer applyQty;

        /**
         * 目的仓库id
         */
        @NotBlank(message = "目的仓库不能为空")
        private String destWarehouseId;

        /**
         * 目的仓库名
         */
        private String destWarehouseName;

        /**
         * 采购组织id
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 采购组织名
         */
        private String purchaseOrgName;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

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
    public static class ViewDTO extends AddDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织名
         */
        private String receiveOrgName;
    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<PurchaseApplicationDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    @Data
    public static class PurchaseSkuQtyDTO{
        /**
         * 来源单据id
         */
        private String sourceId;
        /**
         * 来源单据详情id
         */
        private String sourceDetailId;
        /**
         * skuID
         */
        private String skuId;
        /**
         * 该sku已申请的数量
         */
        private int qty;
    }
}
