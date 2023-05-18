package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 15:45
 */
@Data
@NoArgsConstructor
public class MachineSubComponentsDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO  {

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
        @NotNull(message = "子件数量不能为空")
        @Min(value = 1,message = "子件数量最小值为1")
        @Max(value = 999999999,message = "子件数量最大值为999999999")
        private Integer qty;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
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
        @NotBlank(message = "子件明细id不能为空")
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
         * 单位
         */
        private String unit;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;

    }
}
