package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 预入库单详情 DTO
 *
 * @author auto
 * @since 2026-06-30
 */
public class SoReturnPrestockDetailDTO {

    private SoReturnPrestockDetailDTO() {
        throw new IllegalStateException("Utility SoReturnPrestockDetailDTO class");
    }

    // ===================== 新增 =====================

    /**
     * 新增详情行入参
     */
    @Data
    @NoArgsConstructor
    public static class Add {

        /** SKU ID */
        private String skuId;

        /** SKU 编码 */
        @NotBlank(message = "SKU编码不能为空")
        private String skuNo;

        /** 产品名称 */
        private String productName;

        /** 产品图片 URL */
        private String productImageUrl;

        /** EAN 码 */
        private String ean;

        /** 退货数量 */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量必须大于0")
        private Integer returnQty;

        /** 实际收货数量 */
        private Integer receiveQty;

        /** 平台订单号 */
        private String platformOrderCode;

        /** 平台字典值 */
        private String dictPlatform;

        /** 备注 */
        private String remark;
    }

    // ===================== 修改 =====================

    /**
     * 修改详情行入参
     */
    @Data
    @NoArgsConstructor
    public static class Update {

        /** 详情行 ID */
        @NotBlank(message = "详情行ID不能为空")
        private String id;

        /** 版本号（乐观锁，必传，用于校验数据是否已被他人修改） */
        @NotNull(message = "版本号不能为空")
        private Integer version;

        /** 实际收货数量 */
        private Integer receiveQty;

        /** 备注 */
        private String remark;
    }

    // ===================== 关联售后单 =====================

    /**
     * 关联售后单入参；支持将一条详情行的部分数量关联到售后单（触发拆行）
     */
    @Data
    @NoArgsConstructor
    public static class LinkAfterSale {

        /** 详情行 ID */
        @NotBlank(message = "详情行ID不能为空")
        private String detailId;

        /** 本次关联数量（≤ 当前行 return_qty）*/
        @NotNull(message = "关联数量不能为空")
        @Min(value = 1, message = "关联数量必须大于0")
        private Integer linkQty;

        /** 售后单 ID */
        @NotBlank(message = "售后单ID不能为空")
        private String afterSaleId;

        /** 售后单号 */
        private String afterSaleCode;

        /** 销售单 ID */
        private String soId;

        /** 销售单号 */
        private String soCode;

        /** 退货单 ID（OMS so_return.id）；大范围模糊匹配选定后写入 */
        private String soReturnId;

        /** 退货单号（OMS so_return.code） */
        private String soReturnCode;

        /** 店铺 ID */
        private String shopId;

        /** 店铺名称 */
        private String shopName;

        /** 销售组织 ID */
        private String salesOrgId;

        /** 销售组织名称 */
        private String salesOrgName;

        /** 销售部门 ID */
        private String salesDeptId;

        /** 销售部门名称 */
        private String salesDeptName;
    }

    // ===================== 关联店铺 =====================

    /**
     * 关联店铺入参；支持将一条详情行的部分数量关联到店铺（触发拆行）
     */
    @Data
    @NoArgsConstructor
    public static class LinkShop {

        /** 详情行 ID */
        @NotBlank(message = "详情行ID不能为空")
        private String detailId;

        /** 本次关联数量（≤ 当前行 return_qty）*/
        @NotNull(message = "关联数量不能为空")
        @Min(value = 1, message = "关联数量必须大于0")
        private Integer linkQty;

        /** 店铺 ID */
        @NotBlank(message = "店铺ID不能为空")
        private String shopId;

        /** 店铺名称 */
        private String shopName;

        /** 平台字典值 */
        private String dictPlatform;

        /** 销售组织 ID */
        private String salesOrgId;

        /** 销售组织名称 */
        private String salesOrgName;

        /** 销售部门 ID */
        private String salesDeptId;

        /** 销售部门名称 */
        private String salesDeptName;
    }

    // ===================== 详情展示 =====================

    /**
     * 详情行查询出参
     */
    @Data
    @NoArgsConstructor
    public static class View {

        private String id;

        /** 版本号（乐观锁，修改时需原样带回） */
        private Integer version;

        /** 主表 ID */
        private String mainId;

        /** 拆分来源详情行 ID */
        private String parentDetailId;

        /** 售后单 ID */
        private String afterSaleId;

        /** 售后单号 */
        private String afterSaleCode;

        /** 平台订单号 */
        private String platformOrderCode;

        /** 平台字典值 */
        private String dictPlatform;

        /** 销售单 ID */
        private String soId;

        /** 销售单号 */
        private String soCode;

        /** 退货单 ID（OMS so_return.id） */
        private String soReturnId;

        /** 退货单号（OMS so_return.code） */
        private String soReturnCode;

        /** 生成的退货入库单 ID；认领关联成功后系统回写 */
        private String returnInstockId;

        /** 生成的退货入库单号；认领关联成功后系统回写 */
        private String returnInstockCode;

        /** 店铺 ID */
        private String shopId;

        /** 店铺名称 */
        private String shopName;

        /** SKU ID */
        private String skuId;

        /** SKU 编码 */
        private String skuNo;

        /** 产品名称 */
        private String productName;

        /** 产品图片 URL */
        private String productImageUrl;

        /** EAN 码 */
        private String ean;

        /** 退货数量 */
        private Integer returnQty;

        /** 实际收货数量 */
        private Integer receiveQty;

        /** 已认领数量 */
        private Integer claimedQty;

        /** 关联状态 */
        private String linkStatus;

        /** 关联状态名称 */
        private String linkStatusName;

        /** 销售组织 ID */
        private String salesOrgId;

        /** 销售组织名称 */
        private String salesOrgName;

        /** 销售部门 ID */
        private String salesDeptId;

        /** 销售部门名称 */
        private String salesDeptName;

        /** 备注 */
        private String remark;
    }
}
