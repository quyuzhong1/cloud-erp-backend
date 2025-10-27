package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/10 12:15
 */
@Data
@NoArgsConstructor
public class OtherOutstockDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * SKU
         */
        @NotBlank(message = "SKU不能为空")
        private String skuNo;
        /**
         * 产品单位
         */
        @NotBlank(message = "产品单位不能为空")
        private String unitName;
        /**
         * 实发数量
         */
        @NotNull(message = "实发数量不能为空")
        @Min(value = 1,message = "实发数量最小值为1")
        @Max(value = 999999999,message = "实发数量最大值为999999999")
        private Integer actualQty;

        /**
         * 库位id
         */
        private String warehouseLocation;

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
    public static class ViewDTO extends UpdateDTO {

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;
    }
}
