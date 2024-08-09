package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * <p>
 * 仓位移动明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
@Data
@NoArgsConstructor
public class WarehouseLocationMoveDetailDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * sku表id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * sku图片
         */
        private String skuImg;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 取货仓位
         */
        private String outWarehouseLocation;

        /**
         * 取货仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 上架仓位
         */
        private String inWarehouseLocation;

        /**
         * 上架仓位名称
         */
        private String inWarehouseLocationName;
        /**
         * 取货仓位库存状态
         */
        private String outInventoryStatus;
        /**
         * 上架仓位库存状态
         */
        private String inInventoryStatus;

        /**
         * 移动数量
         */
        private Integer qty;
        /**
         * 仓库
         */
        private String WarehouseId;
        /**
         * 仓库
         */
        private String WarehouseName;
    }

    @Data
    @NoArgsConstructor
    public static class ViewQtyDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * sku表id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * sku图片
         */
        private String skuImg;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 取货仓位
         */
        private String outWarehouseLocation;

        /**
         * 取货仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 上架仓位
         */
        private String inWarehouseLocation;

        /**
         * 上架仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 移动数量
         */
        private Integer qty;
        /**
         * 实际库存
         */
        private Integer realQty;
        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 备注
         */
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        public static WarehouseLocationMoveDetailDTO.AddDTO getLocationMoveDTO(String skuId, String skuNo, String outWarehouseLocation, String inWarehouseLocation, Integer qty, String warehouseId, String sourceDetailId) {
            WarehouseLocationMoveDetailDTO.AddDTO addDTO = new WarehouseLocationMoveDetailDTO.AddDTO();
            addDTO.setSkuId(skuId);
            addDTO.setSkuNo(skuNo);
            addDTO.setOutWarehouseLocation(outWarehouseLocation);
            addDTO.setInWarehouseLocation(inWarehouseLocation);
            addDTO.setQty(qty);
            addDTO.setWarehouseId(warehouseId);
            addDTO.setSourceDetailId(sourceDetailId);
            return addDTO;
        }
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
        /**
         * 备注
         */
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 主表id
         */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19, message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
         * sku表id
         */
        @NotBlank(message = "sku表id不能为空")
        @Size(max = 19, message = "sku表id最大长度不能超过19位")
        private String skuId;

        /**
         * sku编号
         */
        @NotBlank(message = "sku编号不能为空")
        @Size(max = 255, message = "sku编号最大长度不能超过255位")
        private String skuNo;

        /**
         * 取货仓位
         */
        @NotBlank(message = "取货仓位不能为空")
        @Size(max = 50, message = "取货仓位最大长度不能超过50位")
        private String outWarehouseLocation;
        /**
         * 取货仓位库存状态
         */
        private String outInventoryStatus;

        /**
         * 上架仓位
         */
        @NotBlank(message = "上架仓位不能为空")
        @Size(max = 50, message = "上架仓位最大长度不能超过50位")
        private String inWarehouseLocation;

        /**
         * 上架仓位库存状态
         */
        private String inInventoryStatus;

        /**
         * 移动数量
         */
        @NotNull(message = "移动数量不能为空")
        @Min(value = 0, message = "移动数量不能小于0")
        @Max(value = 999999999, message = "移动数量最大值为999999999")
        private Integer qty;

        /**
         * 取货仓位
         */
        private String warehouseId;
    }


}