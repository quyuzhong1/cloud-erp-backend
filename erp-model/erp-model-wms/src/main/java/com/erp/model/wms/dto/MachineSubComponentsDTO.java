package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0

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
         * 库位名称
         */
        private String warehouseLocationName;
        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;

        /**
         * 是否是子件子级（作用用于前端判断）
         */
        private Boolean isChild;
        /**
         * 处理类型
         */
        private String handleType;
        /**
         * 处理详情
         */
        private String handleDetail;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;

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
         * 供应商
         */
        private String childSupplierId;

        /**
         * 单位
         */
        private String unit;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;

        /**
         * BOM套装的子件数量
         */
        private Integer itemQty;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 子级SKU数量(前端需要的标识)
         */
        private Integer childLength;
        /**
         * 子级SKU是否显示(前端需要的标识)
         */
        private Boolean childHidden;
    }

    @Data
    @NoArgsConstructor
    public static class BomViewDTO  {
        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 集合
         */
        private List<ViewDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class HandleDetailDTO  {

        /**
         * 子级SKU仓库id
         */
        private String childWarehouseId;
        /**
         * 子级SKU供应商id
         */
        private String childSupplierId;
        /**
         * 子级SKU仓位
         */
        private String childWarehouseLocation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewBomParamDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * bom版本
         */
        private String bomVersion;
    }
}
