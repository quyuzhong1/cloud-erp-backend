package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 14:04
 */
@Data
@NoArgsConstructor
public class MachineDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

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
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1,message = "数量最小值为1")
        @Max(value = 999999999,message = "数量最大值为999999999")
        private Integer qty;

        /**
         * 库位id
         */
        private String warehouseLocation;

        /**
         * 参照版本
         */
        private Integer referenceVersion;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{

        /**
         * 子件信息
         */
        @NotEmpty(message = "子件明细不能为空")
        @Valid
        private List<MachineSubComponentsDTO.AddDTO> addList;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 子件信息
         */
        @NotEmpty(message = "子件明细不能为空")
        @Valid
        private List<MachineSubComponentsDTO.UpdateDTO> updateList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;

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

        /**
         * 明细子件
         */
        private List<MachineSubComponentsDTO.ViewDTO> addList;
    }
}
