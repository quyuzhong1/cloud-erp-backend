package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 预入库单详情表
 * </p>
 *
 * @author auto
 * @since 2026-06-30
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_return_prestock_detail")
public class SoReturnPrestockDetailEntity extends BaseEntity<SoReturnPrestockDetailEntity> {

    public static final String MAIN_ID = "main_id";
    public static final String PARENT_DETAIL_ID = "parent_detail_id";
    public static final String AFTER_SALE_ID = "after_sale_id";
    public static final String AFTER_SALE_CODE = "after_sale_code";
    public static final String PLATFORM_ORDER_CODE = "platform_order_code";
    public static final String DICT_PLATFORM = "dict_platform";
    public static final String SO_ID = "so_id";
    public static final String SO_CODE = "so_code";
    public static final String SO_RETURN_ID = "so_return_id";
    public static final String SO_RETURN_CODE = "so_return_code";
    public static final String SHOP_ID = "shop_id";
    public static final String SHOP_NAME = "shop_name";
    public static final String SKU_ID = "sku_id";
    public static final String SKU_NO = "sku_no";
    public static final String PRODUCT_NAME = "product_name";
    public static final String RECEIVED_QTY = "received_qty";
    public static final String CLAIMED_QTY = "claimed_qty";
    public static final String CLAIM_STATUS = "claim_status";
    public static final String SALES_ORG_ID = "sales_org_id";
    public static final String SALES_DEPT_ID = "sales_dept_id";
    public static final String SELLER_ID = "seller_id";
    public static final String SELLER_NAME = "seller_name";
    /**
     * 主表 ID（so_return_prestock.id）
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 拆分来源详情行 ID；原始创建时为空字符串，拆分行记录原行 ID
     */
    @TableField("parent_detail_id")
    private String parentDetailId;
    /**
     * 售后单 ID
     */
    @TableField("after_sale_id")
    private String afterSaleId;
    /**
     * 售后单号
     */
    @TableField("after_sale_code")
    private String afterSaleCode;
    /**
     * 平台订单号；不代表本行数据的来源渠道，而是由【关联售后单】操作写入，
     * 取自所关联售后单（B2C/B2B售后订单）自身的平台订单号；
     * 关联店铺或未关联前为空字符串（店铺不对应具体订单）
     */
    @TableField("platform_order_code")
    private String platformOrderCode;

    // ===================== 常量字段 =====================
    /**
     * 平台字典值；不代表本行数据的来源渠道，而是由【关联】操作决定：
     * 关联售后单时取该售后单（B2C/B2B售后订单）自身的 dict_platform；
     * 关联店铺时取该店铺所属的平台；未关联前为空字符串
     */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
     * 销售单 ID
     */
    @TableField("so_id")
    private String soId;
    /**
     * 销售单号
     */
    @TableField("so_code")
    private String soCode;
    /**
     * 退货单 ID（OMS so_return.id）；通过大范围模糊匹配找出可匹配的售后单后，选定确认关联时写入
     */
    @TableField("so_return_id")
    private String soReturnId;
    /**
     * 退货单号（OMS so_return.code）；选定后即为该明细行的关联退货单
     */
    @TableField("so_return_code")
    private String soReturnCode;
    /**
     * 店铺 ID
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;
    /**
     * SKU ID
     */
    @TableField("sku_id")
    private String skuId;
    /**
     * SKU 编码
     */
    @TableField("sku_no")
    private String skuNo;
    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;
    /**
     * 产品图片 URL
     */
    @TableField("product_image_url")
    private String productImageUrl;
    /**
     * EAN 码
     */
    @TableField("ean")
    private String ean;
    /**
     * 实际收货数量（预入库单明细唯一数量字段；关联/拆行/认领均以此为准）
     */
    @TableField("received_qty")
    private Integer receivedQty;
    /**
     * 已认领数量
     */
    @TableField("claimed_qty")
    private Integer claimedQty;
    /**
     * 关联状态：UNLINKED=未关联，LINKED=已关联
     */
    @TableField("claim_status")
    private String claimStatus;
    /**
     * 销售组织 ID
     */
    @TableField("sales_org_id")
    private String salesOrgId;
    /**
     * 销售组织名称
     */
    @TableField("sales_org_name")
    private String salesOrgName;
    /**
     * 销售部门 ID
     */
    @TableField("sales_dept_id")
    private String salesDeptId;
    /**
     * 销售部门名称
     */
    @TableField("sales_dept_name")
    private String salesDeptName;
    /**
     * 销售员 ID
     */
    @TableField("seller_id")
    private String sellerId;
    /**
     * 销售员名称
     */
    @TableField("seller_name")
    private String sellerName;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
