package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * DWD头程发货签收变更记录(包含期初/调整)
 * </p>
 *
 * @author Jim
 * @since 2025-11-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dwd_first_mile_shipment_change_f")
public class DwdFirstMileShipmentChangeFEntity extends BaseEntity<DwdFirstMileShipmentChangeFEntity> {

    /**
    * 来源系统：amazon
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 来源平台
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 平台账号编码
    */
    @TableField("account_code")
    private String accountCode;
    /**
    * 核对周期
    */
    @TableField("check_month")
    private String checkMonth;
    /**
    * 核对周期(时间格式)
    */
    @TableField("check_month_query")
    private LocalDateTime checkMonthQuery;
    /**
    * 店铺ID/授权ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 业务类型:firstMileInit=期初,firstMileAdjust=调整
    */
    @TableField("bill_topic")
    private String billTopic;
    /**
    * 平台货件ID
    */
    @TableField("platform_shipment_id")
    private String platformShipmentId;
    /**
    * 平台货件单号
    */
    @TableField("platform_shipment_code")
    private String platformShipmentCode;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 客户id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 客户姓名
    */
    @TableField("customer_name")
    private String customerName;
    /**
    * 货件状态
    */
    @TableField("shipment_status")
    private String shipmentStatus;
    /**
    * 货件创建时间
    */
    @TableField("shipment_create_time")
    private LocalDateTime shipmentCreateTime;
    /**
    * 目的仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 目的仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 在途仓库id
    */
    @TableField("intransit_warehouse_id")
    private String intransitWarehouseId;
    /**
    * 在途仓库名称
    */
    @TableField("intransit_warehouse_name")
    private String intransitWarehouseName;
    /**
    * 平台产品id（ASIN）
    */
    @TableField("platform_spu_no")
    private String platformSpuNo;
    /**
    * 平台sku（MSKU）/销售平台SKU
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * FNSKU/平台库存SKU
    */
    @TableField("platform_stock_sku")
    private String platformStockSku;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * ERP SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 变更数量
    */
    @TableField("change_qty")
    private Integer changeQty;
    /**
     * 备份
     */
    @TableField("remark")
    private String remark;


    public static final String SOURCE_SYSTEM = "source_system";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String ACCOUNT_CODE = "account_code";

    public static final String CHECK_MONTH = "check_month";

    public static final String CHECK_MONTH_QUERY = "check_month_query";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String BILL_TOPIC = "bill_topic";

    public static final String PLATFORM_SHIPMENT_ID = "platform_shipment_id";

    public static final String PLATFORM_SHIPMENT_CODE = "platform_shipment_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String SHIPMENT_STATUS = "shipment_status";

    public static final String SHIPMENT_CREATE_TIME = "shipment_create_time";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String INTRANSIT_WAREHOUSE_ID = "intransit_warehouse_id";

    public static final String INTRANSIT_WAREHOUSE_NAME = "intransit_warehouse_name";

    public static final String PLATFORM_SPU_NO = "platform_spu_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_STOCK_SKU = "platform_stock_sku";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String CHANGE_QTY = "change_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}