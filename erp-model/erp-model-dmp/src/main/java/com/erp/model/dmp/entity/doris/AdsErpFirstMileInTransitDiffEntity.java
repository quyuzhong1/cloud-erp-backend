package com.erp.model.dmp.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
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
 * 平台在途报告
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ads_erp_first_mile_intransit_diff")
public class AdsErpFirstMileInTransitDiffEntity extends BaseEntity<AdsErpFirstMileInTransitDiffEntity> {

    /**
    * 唯一标识md5（如main_id+sku_no）
    */
    @TableField("unique_code")
    private String uniqueCode;
    /**
    * 数据字段md5值（用于去重或校验）
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 来源系统：amazon
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 平台账号编码
    */
    @TableField("account_code")
    private String accountCode;
    /**
    * 店铺ID/授权ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 业务类型
    */
    @TableField("bill_topic")
    private String billTopic;
    /**
    * 流程id
    */
    @TableField("flow_id")
    private String flowId;
    /**
    * ETL状态 ready=可处理，unready=未处理
    */
    @TableField("etl_status")
    private String etlStatus;
    /**
    * 节点id
    */
    @TableField("node_id")
    private String nodeId;
    /**
    * 实例id
    */
    @TableField("instance_id")
    private String instanceId;
    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 是否最后一次更新记录
    */
    @TableField("last_update_flag")
    private String lastUpdateFlag;
    /**
    * 核算月份（YYYY-MM）
    */
    @TableField("check_month")
    private String checkMonth;
    /**
     * 核对周期页面查询
     */
    @TableField("check_month_query")
    private String checkMonthQuery;
    /**
    * 货件ID
    */
    @TableField("shipment_id")
    private String shipmentId;
    /**
    * 货件单号
    */
    @TableField("shipment_code")
    private String shipmentCode;
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
    private Date shipmentCreateTime;
    /**
    * 货件签收时间
    */
    @TableField("shipment_receive_time")
    private Date shipmentReceiveTime;
    /**
    * 货件调整时间
    */
    @TableField("shipment_adjust_time")
    private Date shipmentAdjustTime;
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
    * 货件明细id
    */
    @TableField("shipment_detail_id")
    private String shipmentDetailId;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 收货数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 收发差异
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 期初在途数量
    */
    @TableField("init_transit_qty")
    private Integer initTransitQty;
    /**
    * 本期发货数量
    */
    @TableField("current_delivery_qty")
    private Integer currentDeliveryQty;
    /**
    * 本期签收数量
    */
    @TableField("current_receive_qty")
    private Integer currentReceiveQty;
    /**
    * 期末在途数量
    */
    @TableField("end_period_transit_qty")
    private Integer endPeriodTransitQty;
    /**
    * 期末在途调整数量
    */
    @TableField("end_period_transit_adjust_qty")
    private Integer endPeriodTransitAdjustQty;
    /**
    * 期末在途数量（调整后）
    */
    @TableField("after_end_period_transit_qty")
    private Integer afterEndPeriodTransitQty;
    /**
    * 调整原因
    */
    @TableField("adjust_reason")
    private String adjustReason;
    /**
    * 调整时间
    */
    @TableField("adjust_time")
    private Date adjustTime;
    /**
    * 调整人名称
    */
    @TableField("adjust_user_name")
    private String adjustUserName;
    /**
    * 调整人id
    */
    @TableField("adjust_user_id")
    private String adjustUserId;
    /**
    * 备注或附加说明
    */
    @TableField("remark")
    private String remark;


    public static final String UNIQUE_CODE = "unique_code";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String ACCOUNT_CODE = "account_code";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String BILL_TOPIC = "bill_topic";

    public static final String FLOW_ID = "flow_id";

    public static final String ETL_STATUS = "etl_status";

    public static final String NODE_ID = "node_id";

    public static final String INSTANCE_ID = "instance_id";

    public static final String TASK_ID = "task_id";

    public static final String SOURCE_ID = "source_id";

    public static final String LAST_UPDATE_FLAG = "last_update_flag";

    public static final String CHECK_MONTH = "check_month";

    public static final String SHIPMENT_ID = "shipment_id";

    public static final String SHIPMENT_CODE = "shipment_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String SHIPMENT_STATUS = "shipment_status";

    public static final String SHIPMENT_CREATE_TIME = "shipment_create_time";

    public static final String SHIPMENT_RECEIVE_TIME = "shipment_receive_time";

    public static final String SHIPMENT_ADJUST_TIME = "shipment_adjust_time";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String INTRANSIT_WAREHOUSE_ID = "intransit_warehouse_id";

    public static final String INTRANSIT_WAREHOUSE_NAME = "intransit_warehouse_name";

    public static final String PLATFORM_SPU_NO = "platform_spu_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_STOCK_SKU = "platform_stock_sku";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SHIPMENT_DETAIL_ID = "shipment_detail_id";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String INIT_TRANSIT_QTY = "init_transit_qty";

    public static final String CURRENT_DELIVERY_QTY = "current_delivery_qty";

    public static final String CURRENT_RECEIVE_QTY = "current_receive_qty";

    public static final String END_PERIOD_TRANSIT_QTY = "end_period_transit_qty";

    public static final String END_PERIOD_TRANSIT_ADJUST_QTY = "end_period_transit_adjust_qty";

    public static final String AFTER_END_PERIOD_TRANSIT_QTY = "after_end_period_transit_qty";

    public static final String ADJUST_REASON = "adjust_reason";

    public static final String ADJUST_TIME = "adjust_time";

    public static final String ADJUST_USER_NAME = "adjust_user_name";

    public static final String ADJUST_USER_ID = "adjust_user_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}