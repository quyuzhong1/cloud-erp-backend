package com.erp.model.wms.dto;

import com.common.business.dto.base.SuperDTO;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/10 14:22
 */
@Data
@NoArgsConstructor
public class TransferInfoDetailDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO extends SuperDTO {

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String  skuId;

        /**
         * SKU
         */
        private String  skuNo;
        /**
         * 单位
         */
        @NotBlank(message = "单位不能为空")
        private String unitName;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Min(value = 1,message = "数量最小值为1")
        @Max(value = 99999999,message = "数量最大值为99999999")
        private Integer  qty;

        /**
         * 调出仓库id
         */
        @NotBlank(message = "调出仓库不能为空")
        private String outWarehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 调入仓库id
         */
        @NotBlank(message = "调入仓库不能为空")
        private String inWarehouseId;

        /**
         * 调入仓位id
         */
        private String  inWarehouseLocation;

        /**
         * 调出仓位id
         */
        private String  outWarehouseLocation;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String  remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 调出库存状态: usable可用/frozen冻结/defectiveProduct不良品（默认可用）
         * 仅允许 InventoryStatusEnum 中的 USABLE/FROZEN/DEFECTIVE_PRODUCT 三值；为空时按 DB 默认可用。
         */
        @StateEnumValue(strValues = {"usable", "frozen", "defectiveProduct"}, message = "调出库存状态值有误")
        private String outInventoryStatus;

        /**
         * 调入库存状态: usable可用/frozen冻结/defectiveProduct不良品（默认可用）
         * 仅允许 InventoryStatusEnum 中的 USABLE/FROZEN/DEFECTIVE_PRODUCT 三值；为空时按 DB 默认可用。
         */
        @StateEnumValue(strValues = {"usable", "frozen", "defectiveProduct"}, message = "调入库存状态值有误")
        private String inInventoryStatus;
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
         * spu编号
         */
        private String spuNo;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;

        /**
         * 调出仓库
         */
        private String outWarehouseName;

        /**
         * 调入仓库
         */
        private String inWarehouseName;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

        /**
         * 调入仓位
         */
        private String inWarehouseLocation;

        /**
         * 调出仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 调入仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 调出库存状态(usable可用/frozen冻结/defectiveProduct不良品)
         */
        private String outInventoryStatus;

        /**
         * 调出库存状态名称
         */
        private String outInventoryStatusName;

        /**
         * 调入库存状态(usable可用/frozen冻结/defectiveProduct不良品)
         */
        private String inInventoryStatus;

        /**
         * 调入库存状态名称
         */
        private String inInventoryStatusName;
    }
    @Data
    @NoArgsConstructor
    public static class ApproveDTO extends AddDTO {
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
