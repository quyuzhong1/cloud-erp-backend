package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 10:34
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class addDTO {
        /**
         * 采购申请id
         */
        private String purchaseApplicationId;

        /**
         * skuId
         */
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
         * 单箱数量
         */
        @Min(value = 0,message = "计划备货数量最小值为0")
        @Max(value = 99999999,message = "计划备货数量最大值为99999999")
        private Integer unitQty;

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
        @NotEmpty(message = "申请数量不能为空")
        @Min(value = 1,message = "申请数量最小值为1")
        @Max(value = 99999999,message = "申请数量最大值为99999999")
        private Integer applyQty;

        /**
         * 目的仓库id
         */
        @NotBlank(message = "目的仓库不能为空")
        private String destWarehouseId;

        /**
         * 采购组织id
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 收料组织id
         */
        @NotBlank(message = "收料组织不能为空")
        private String receiveOrgId;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class updateDTO extends addDTO {
        /**
         * 主键id
         */
        private String id;
    }

}
