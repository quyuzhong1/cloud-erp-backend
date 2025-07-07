package com.erp.model.scm.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 供应商采购数量请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-18
*/
@Data
@NoArgsConstructor
public class SupplierPurchaseQuantityDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * sku_id
        */
        private String skuId;

        /**
        * sku_no
        */
        private String skuNo;

        /**
        * 供应商数量
        */
        private Integer supplierQty;

        /**
        * sku总数量
        */
        private Integer skuTotalQty;

        /**
        * 采购比例
        */
        private BigDecimal purchaseRatio;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 64,message = "供应商id最大长度不能超过64位")
        private String supplierId;

        /**
        * 供应商名称
        */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 64,message = "供应商名称最大长度不能超过64位")
        private String supplierName;

        /**
        * sku_id
        */
        @NotBlank(message = "sku_id不能为空")
        @Size(max = 64,message = "sku_id最大长度不能超过64位")
        private String skuId;

        /**
        * 供应商数量
        */
        @NotNull(message = "供应商数量不能为空")
        private Integer supplierQty;

        /**
        * sku总数量
        */
        @NotNull(message = "sku总数量不能为空")
        private Integer skuTotalQty;

        /**
        * 采购比例
        */
        @NotNull(message = "采购比例不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购比例整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchaseRatio;


    }


}