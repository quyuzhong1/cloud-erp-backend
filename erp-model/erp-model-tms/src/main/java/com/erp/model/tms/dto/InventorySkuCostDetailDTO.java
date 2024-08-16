package com.erp.model.tms.dto;

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
 * SKU成本明细请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
*/
@Data
@NoArgsConstructor
public class InventorySkuCostDetailDTO implements Serializable {




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
        * 备注
        */
        private String remark;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 单位（pcs,ml）
        */
        private String unit;

        /**
        * 产品成本（6位小数）
        */
        private BigDecimal productCost;


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
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 单位（pcs,ml）
        */
        @NotBlank(message = "单位（pcs,ml）不能为空")
        @Size(max = 20,message = "单位（pcs,ml）最大长度不能超过20位")
        private String unit;

        /**
        * 产品成本（6位小数）
        */
        @NotNull(message = "产品成本（6位小数）不能为空")
        @Digits(integer = 12, fraction = 6, message = "产品成本（6位小数）整数位不能超过12位，小数位不能超过6位")
        private BigDecimal productCost;


    }


}