package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 销售订单出库单
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_outstock")
public class SoOutstockEntity extends BaseEntity<SoOutstockEntity> {

    /**
     * code
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;


    /**
     * 销售订单id
     */
    @TableField("so_id")
    private String soId;

    /**
     * 承运商 来源供应商
     */
    @TableField("carrier_id")
    private String carrierId;

    /**
     * 作废状态
     * true 作废
     * false 未作废
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 销售员id
     */
    @TableField("seller_id")
    private String sellerId;

    /**
     * 销售员
     */
    @TableField("seller_name")
    private String sellerName;


    /**
     * 销售订单code
     */
    @TableField("so_code")
    private String soCode;

    /**
     * 库存组织
     */
    @TableField("warehouse_org_id")
    private String warehouseOrgId;

    /**
     * 库存组织名
     */
    @TableField("warehouse_org_name")
    private String warehouseOrgName;



    /**
     * 预计发货日期
     */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;

    /**
     * 打包日期
     */
    @TableField("pack_date")
    private LocalDate packDate;

    /**
     * 整单折扣额
     */
    @TableField("total_discount_amount")
    private BigDecimal totalDiscountAmount;

    /**
     * 实际发货日期
     */
    @TableField("actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    /**
     * 运输单号
     */
    @TableField("track_no")
    private String trackNo;

    /**
     * 仓管员
     */
    @TableField("warehouse_keeper_id")
    private String warehouseKeeperId;

    /**
     * 仓管员
     */
    @TableField("warehouse_keeper_name")
    private String warehouseKeeperName;


    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源code
     */
    @TableField("source_code")
    private String sourceCode;


    /**
     * type
     * 单据类型 冗余
     */
    @TableField("order_type")
    private String orderType;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;


    /**
     * 审核人
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 客户id
     */
    @TableField("customer_id")
    private String customerId;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 出库日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 客户订单号
     */
    @TableField("customer_order_no")
    private String customerOrderNo;


    /**
     * 销售部门id
     */
    @TableField("sales_dept_id")
    private String salesDeptId;


    /**
     * 销售组织id
     */
    @TableField("sales_org_id")
    private String salesOrgId;


    /**
     * 销售组织名
     */
    @TableField("sales_org_name")
    private String salesOrgName;


    /**
     * 客户名
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 国家
     */
    @TableField("country")
    private String country;


    /**
     * 第三方单据编号
     */
    @TableField("third_code")
    private String thirdCode;

    /**
     * 物流渠道id
     */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;

    /**
     * 装箱状态 notPacking：未装箱，packing：已装箱
     * 枚举：PackingTaskStatusEnum
     */
    @TableField("packing_status")
    private String packingStatus;


    /**
     * 报关状态
     * 枚举：DeclareStatusEnum
     */
    @TableField("declare_status")
    private String declareStatus;

    /**
     * 旺店通原单创建时间
     */
    @TableField("created")
    private LocalDateTime created;

    /**
     * 批次号，下推时生成
     */
    @TableField("batch_no")
    private String batchNo;

    /**
     * 物流渠道名称
     */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;

    /**
     * 订单标签
     */
    @TableField("trade_label")
    private String tradeLabel;

    @TableField(exist = false)
    private List<SoOutstockDetailEntity> detailList;

    

    public static final String APPROVE_STATUS = "approve_status";

    

    public static final String SO_ID = "so_id";

    public static final String CARRIER_ID = "carrier_id";

    public static final String CARRIER_NAME = "carrier_name";

    public static final String SO_CODE = "so_code";

    public static final String DELIVERY_ORG_ID = "delivery_org_id";

    public static final String DELIVERY_ORG_NAME = "delivery_org_name";

    public static final String REQUIRE_DATE = "require_date";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String PACK_DATE = "pack_date";

    public static final String ACTUAL_DELIVERY_DATE = "actual_delivery_date";

    public static final String TRACK_NO = "track_no";

    public static final String WAREHOUSE_KEEPER_ID = "warehouse_keeper_id";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String CONSUMER_ID  = "consumer_id ";

    public static final String CONSUMER_NAME = "consumer_name";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVER_ADDRESS = "receiver_address";

    public static final String DELIVERY_MODE_DICT = "delivery_mode_dict";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
