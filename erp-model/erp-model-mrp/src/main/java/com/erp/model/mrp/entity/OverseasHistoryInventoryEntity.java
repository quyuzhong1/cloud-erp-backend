package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 海外仓库存
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("overseas_history_inventory")
@EqualsAndHashCode(callSuper = true)
public class OverseasHistoryInventoryEntity extends BaseEntity<OverseasHistoryInventoryEntity> {

    private static final long serialVersionUID = 1402809218296720044L;
    /**
     * 平台仓库编码
     */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
     * 平台类型: goodcang=谷仓，iml=艾姆勒
     */
    @TableField("dict_platform")
    private String dictPlatform;

    /**
     * 仓库名称
     */
    @TableField("name")
    private String name;

    /**
     * ERP系统产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * ERP的SKU
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * ERP的SKU ID
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 发货在途数量
     */
    @TableField("deliver_onway_qty")
    private Integer deliverOnwayQty;
    /**
     * 待上架数量
     */
    @TableField("pending_qty")
    private Integer pendingQty;
    /**
     * 可售数量
     */
    @TableField("sellable_qty")
    private Integer sellableQty;
    /**
     * 不可售数量
     */
    @TableField("unsellable_qty")
    private Integer unsellableQty;
    /**
     * 待出库数量
     */
    @TableField("reserved_qty")
    private Integer reservedQty;
    /**
     * 尾程在途
     */
    @TableField("onway_qty")
    private Integer onwayQty;
    /**
     * 缺货数量
     */
    @TableField("lack_qty")
    private Integer lackQty;
    /**
     * 冻结数量
     */
    @TableField("frozen_qty")
    private Integer frozenQty;
    /**
     * 历史出库数量
     */
    @TableField("shipped_qty")
    private Integer shippedQty;


    /**
     * 平台下载更新时间
     */
    @TableField("download_time")
    private LocalDateTime downloadTime;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 仓库id
     */
    @TableField(exist = false)
    private String warehouseId;


    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String NAME = "name";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String PLATFORM_SKU_NAME = "platform_sku_name";

    public static final String PRODUCT_NAME = "product_name";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String DELIVER_ONWAY_QTY = "deliver_onway_qty";

    public static final String PENDING_QTY = "pending_qty";

    public static final String SELLABLE_QTY = "sellable_qty";

    public static final String UNSELLABLE_QTY = "unsellable_qty";

    public static final String RESERVED_QTY = "reserved_qty";

    public static final String ONWAY_QTY = "onway_qty";

    public static final String LACK_QTY = "lack_qty";

    public static final String FROZEN_QTY = "frozen_qty";

    public static final String SHIPPED_QTY = "shipped_qty";

    public static final String DOWNLOAD_TIME = "download_time";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
