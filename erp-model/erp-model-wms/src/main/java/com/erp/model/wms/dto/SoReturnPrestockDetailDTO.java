package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

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

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU 编码
         */
        @NotBlank(message = "SKU编码不能为空")
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片 URL
         */
        private String productImageUrl;

        /**
         * EAN 码
         */
        private String ean;

        /**
         * 实际收货数量
         */
        @NotNull(message = "实际收货数量不能为空")
        @Min(value = 1, message = "实际收货数量必须大于0")
        private Integer receiveQty;

        /**
         * 备注
         */
        private String remark;
    }

    // ===================== 由退货入库单表单创建 =====================

    /**
     * 由退货入库单表单创建预入库单-详情行入参
     */
    @Data
    @NoArgsConstructor
    public static class FromInstock {

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU 编码
         */
        @NotBlank(message = "SKU编码不能为空")
        private String skuNo;

        /**
         * 实退数量；写入预入库单明细的【实际收货数量】
         * （手动创建时该行数据尚未与售后单关联，统一按本行实退数量落库）
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 1, message = "实退数量必须大于0")
        private Integer realQty;

        /**
         * 仓库 ID；退货入库单表单按明细行填写仓库，预入库单主表仅支持单一仓库。
         * 主表 {@code SoReturnPrestockDTO.FromInstock#warehouseId} 未直接传入时，
         * 由服务端汇总本字段推导主表仓库
         */
        private String warehouseId;

        /**
         * 退货类型字典值；退货入库单表单按明细行填写退货类型，预入库单主表仅支持单一退货类型，
         * 服务端汇总本字段推导主表退货类型（各行不一致时拒绝创建）
         */
        private String returnTypeDict;

        /**
         * 备注
         */
        private String remark;
    }

    // ===================== 修改 =====================

    /**
     * 修改详情行入参
     */
    @Data
    @NoArgsConstructor
    public static class Update {

        /**
         * 详情行 ID
         */
        @NotBlank(message = "详情行ID不能为空")
        private String id;

        /**
         * 版本号（乐观锁，必传，用于校验数据是否已被他人修改）
         */
        @NotNull(message = "版本号不能为空")
        private Integer version;

        /**
         * 实际收货数量
         */
        private Integer receiveQty;

        /**
         * 备注
         */
        private String remark;
    }

    // ===================== 关联售后单 =====================

    /**
     * 关联售后单入参；支持将一条详情行的部分数量关联到售后单（触发拆行）
     */
    @Data
    @NoArgsConstructor
    public static class LinkAfterSale {

        /**
         * 详情行 ID
         */
        @NotBlank(message = "详情行ID不能为空")
        private String detailId;

        /**
         * 本次关联数量（≤ 当前行实际收货数量 receive_qty）
         */
        @NotNull(message = "关联数量不能为空")
        @Min(value = 1, message = "关联数量必须大于0")
        private Integer linkQty;

        /**
         * 售后单 ID
         */
        @NotBlank(message = "售后单ID不能为空")
        private String afterSaleId;

        /**
         * 售后单号
         */
        private String afterSaleCode;

        /**
         * 平台订单号；取自所关联的《B2C/B2B售后订单》自身的平台订单号，不代表本行数据的来源渠道。
         * 由前端在选定售后单后，将该售后单自身的 platform_order_code 一并回传
         */
        private String platformOrderCode;

        /**
         * 平台字典值；取自所关联的《B2C/B2B售后订单》的平台字段，不代表本行数据的来源渠道。
         * 由前端在选定售后单后，将该售后单自身的 dict_platform 一并回传
         */
        @NotBlank(message = "平台不能为空")
        private String dictPlatform;

        /**
         * 销售单 ID
         */
        private String soId;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 退货单 ID（OMS so_return.id）；大范围模糊匹配选定后写入
         */
        private String soReturnId;

        /**
         * 退货单号（OMS so_return.code）
         */
        private String soReturnCode;

        /**
         * 店铺 ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 销售组织 ID
         */
        private String salesOrgId;

        /**
         * 销售组织名称
         */
        private String salesOrgName;

        /**
         * 销售部门 ID
         */
        private String salesDeptId;

        /**
         * 销售部门名称
         */
        private String salesDeptName;

        /**
         * 销售员 ID
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;
    }

    // ===================== 确认关联售后单（批量） =====================

    /**
     * 确认关联售后单入参（预入库单维度批量关联）。
     * <p>页面点击"确定关联"时提交：afterSaleList 为在候选售后单列表
     * （{@code SoReturnController.pagingLinkAfterSale}）中勾选的售后单明细行。
     * 服务端按 SKU 将本次勾选的退货明细数量与预入库单未关联明细数量比较：</p>
     * <ul>
     *   <li>数量种类完全一致：完成关联；</li>
     *   <li>预入库单明细多于退货明细：仅关联能匹配的 SKU（部分关联）；</li>
     *   <li>退货明细超过预入库单（SKU 种类或数量超出）：整批拒绝，提示调配售后退货单后再关联。</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    public static class ConfirmLinkAfterSale {

        /**
         * 预入库单主表 ID（so_return_prestock.id）
         */
        @NotBlank(message = "预入库单ID不能为空")
        private String mainId;

        /**
         * 本次勾选的候选售后单明细行列表
         */
        @NotEmpty(message = "关联的售后单不能为空")
        @Valid
        private List<AfterSaleItem> afterSaleList;
    }

    /**
     * 勾选的候选售后单明细行；字段来源于 {@code SoReturnDTO.LinkAfterSaleView}。
     */
    @Data
    @NoArgsConstructor
    public static class AfterSaleItem {

        /**
         * 售后单（退货单）ID；对应 {@code LinkAfterSaleView.id}（so_return.id / so_b2c_return.id）。
         * 本域中"售后单"即 OMS 退货单，故同时写入 afterSaleId 与 soReturnId
         */
        @NotBlank(message = "售后单ID不能为空")
        private String afterSaleId;

        /**
         * 售后单号（退货单号）；对应 {@code LinkAfterSaleView.afterSaleCode}
         */
        private String afterSaleCode;

        /**
         * 售后单明细 ID；对应 {@code LinkAfterSaleView.detailId}，仅作溯源
         */
        private String detailId;

        /**
         * 销售单 ID；对应 {@code LinkAfterSaleView.soId}（B2B 为 so_return.source_id，B2C 为 so_b2c_return.so_id）
         */
        private String soId;

        /**
         * 销售单号；对应 {@code LinkAfterSaleView.soCode}
         */
        private String soCode;

        /**
         * 平台订单号；对应 {@code LinkAfterSaleView.platformOrderNo}
         */
        private String platformOrderCode;

        /**
         * 平台字典值；对应 {@code LinkAfterSaleView.platform}（B2B 售后单为空）
         */
        private String dictPlatform;

        /**
         * 店铺/客户 ID；对应 {@code LinkAfterSaleView.shopId}（B2B 为客户 ID）
         */
        private String shopId;

        /**
         * 店铺/客户 名称；对应 {@code LinkAfterSaleView.shopName}（B2B 为客户名称）
         */
        private String shopName;

        /**
         * 退货类型字典值；对应 {@code LinkAfterSaleView.returnType}，生成退货入库单时带入明细
         */
        private String returnType;

        /**
         * 退货原因字典值；对应 {@code LinkAfterSaleView.returnReason}，生成退货入库单时带入明细
         */
        private String returnReason;

        /**
         * SKU ID；用于与预入库单未关联明细行按 SKU 匹配，并回填预入库单明细
         * （三无包裹创建时 sku_id 可能为空），保证生成退货入库单及库存联动时 sku_id 非空
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * SKU 编码；用于与预入库单未关联明细行按 SKU 匹配
         */
        @NotBlank(message = "SKU编码不能为空")
        private String skuNo;

        /**
         * 本条售后单退货数量；对应 {@code LinkAfterSaleView.returnQty}
         */
        @NotNull(message = "退货数量不能为空")
        @Min(value = 1, message = "退货数量必须大于0")
        private Integer returnQty;
    }

    // ===================== 关联店铺 =====================

    /**
     * 批量关联店铺入参；ids 为预入库单主表 ID 列表，支持一次选中多张预入库单关联到同一店铺，
     * 不区分 B2B / B2C 单据类型。
     */
    @Data
    @NoArgsConstructor
    public static class LinkShop {

        /**
         * 待关联的预入库单主表 ID 列表
         * <p>单次批量关联在同一全局事务内会为每张预入库单生成退货入库单、平账其他入库单并做多次入库审核，
         * 属于重事务操作，限制单次处理数量以控制全局事务时长与锁持有范围。</p>
         */
        @NotEmpty(message = "预入库单不能为空")
        @Size(max = 50, message = "单次最多关联50张预入库单，请分批操作")
        private List<String> ids;

        /**
         * 店铺 ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 平台字典值；取自所关联店铺自身所属的平台，不代表本行数据的来源渠道。
         * 由前端在选定店铺后，将该店铺自身的 dict_platform 一并回传
         */
        private String dictPlatform;

        /**
         * 销售组织 ID；选定店铺后由前端自动带出并回传
         */
        private String salesOrgId;

        /**
         * 销售组织名称
         */
        private String salesOrgName;

        /**
         * 销售部门 ID；选定店铺后由前端自动带出并回传
         */
        private String salesDeptId;

        /**
         * 销售部门名称
         */
        private String salesDeptName;

        /**
         * 销售员 ID；选定店铺后由前端自动带出并回传
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;
    }

    // ===================== 确认关联店铺（明细维度，逐行选店铺 + 认领数量） =====================

    /**
     * 确认关联店铺入参（预入库单维度，明细逐行选择店铺）。
     * <p>页面点击"确定关联"时提交：shopList 为在产品明细中逐行选择了店铺的未关联行，
     * 每行填写认领数量（默认 = 当前行实际收货数量）。与批量关联店铺 {@link LinkShop} 不同：
     * LinkShop 是把整张预入库单的未关联行整体关联到同一店铺；本接口允许同一张预入库单内
     * 不同明细行分别关联到不同店铺，并支持按认领数量拆行。</p>
     * <ul>
     *   <li>仅未关联行可参与关联；未在 shopList 中出现（未选择店铺）的行保持未关联；</li>
     *   <li>认领数量 = 实际收货数量：整行关联；认领数量 &lt; 实际收货数量：拆行，认领部分独立成行并关联，剩余保持未关联；</li>
     *   <li>关联相同店铺的行合并生成一张《退货入库单》。</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    public static class ConfirmLinkShop {

        /**
         * 预入库单主表 ID（so_return_prestock.id）
         */
        @NotBlank(message = "预入库单ID不能为空")
        private String mainId;

        /**
         * 本次逐行选择了店铺的明细行列表（仅包含已选店铺的行）
         */
        @NotEmpty(message = "关联的明细行不能为空")
        @Valid
        private List<ShopItem> shopList;
    }

    /**
     * 逐行关联店铺明细项；每行对应预入库单一条未关联明细行。
     */
    @Data
    @NoArgsConstructor
    public static class ShopItem {

        /**
         * 预入库单明细行 ID（so_return_prestock_detail.id）
         */
        @NotBlank(message = "详情行ID不能为空")
        private String detailId;

        /**
         * 认领数量（默认 = 当前行实际收货数量，≤ 当前行实际收货数量）；
         * 小于实际收货数量时触发拆行，认领部分独立成行并关联
         */
        @NotNull(message = "认领数量不能为空")
        @Min(value = 1, message = "认领数量必须大于0")
        private Integer claimedQty;

        /**
         * 店铺 ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 平台字典值；取自所关联店铺自身所属平台，由前端在选定店铺后回传
         */
        private String dictPlatform;

        /**
         * 销售组织 ID；选定店铺后由前端自动带出并回传
         */
        private String salesOrgId;

        /**
         * 销售组织名称
         */
        private String salesOrgName;

        /**
         * 销售部门 ID；选定店铺后由前端自动带出并回传
         */
        private String salesDeptId;

        /**
         * 销售部门名称
         */
        private String salesDeptName;

        /**
         * 销售员 ID；选定店铺后由前端自动带出并回传，生成退货入库单时带入
         */
        private String sellerId;

        /**
         * 销售员名称；选定店铺后由前端自动带出并回传
         */
        private String sellerName;
    }

    // ===================== 详情展示 =====================

    /**
     * 详情行查询出参
     */
    @Data
    @NoArgsConstructor
    public static class View {

        private String id;

        /**
         * 版本号（乐观锁，修改时需原样带回）
         */
        private Integer version;

        /**
         * 主表 ID
         */
        private String mainId;

        /**
         * 拆分来源详情行 ID
         */
        private String parentDetailId;

        /**
         * 售后单 ID
         */
        private String afterSaleId;

        /**
         * 售后单号
         */
        private String afterSaleCode;

        /**
         * 平台订单号；由关联售后单操作写入，取自所关联售后单自身的平台订单号；
         * 关联店铺或未关联前为空（店铺不对应具体订单）
         */
        private String platformOrderCode;

        /**
         * 平台字典值；由关联操作写入——关联售后单时取售后单的平台，关联店铺时取店铺所属平台；
         * 未关联前为空
         */
        private String dictPlatform;

        /**
         * 销售单 ID
         */
        private String soId;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 退货单 ID（OMS so_return.id）
         */
        private String soReturnId;

        /**
         * 退货单号（OMS so_return.code）
         */
        private String soReturnCode;

        /**
         * 生成的退货入库单 ID；认领关联成功后系统回写
         */
        private String returnInstockId;

        /**
         * 生成的退货入库单号；认领关联成功后系统回写
         */
        private String returnInstockCode;

        /**
         * 店铺 ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU 编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片 URL
         */
        private String productImageUrl;

        /**
         * EAN 码
         */
        private String ean;

        /**
         * 实际收货数量
         */
        private Integer receiveQty;

        /**
         * 已认领数量
         */
        private Integer claimedQty;

        /**
         * 关联状态
         */
        private String linkStatus;

        /**
         * 关联状态名称
         */
        private String linkStatusName;

        /**
         * 销售组织 ID
         */
        private String salesOrgId;

        /**
         * 销售组织名称
         */
        private String salesOrgName;

        /**
         * 销售部门 ID
         */
        private String salesDeptId;

        /**
         * 销售部门名称
         */
        private String salesDeptName;

        /**
         * 销售员 ID
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 备注
         */
        private String remark;
    }
}
