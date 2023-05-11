package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

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
    private String approveStatus;

    /**
     * 订单类型
     */
    @TableField("type")
    private String type;

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
     * 承运商 来源供应商
     */
    @TableField("carrier_name")
    private String carrierName;

    /**
     * 销售订单code
     */
    @TableField("so_code")
    private String soCode;

    /**
     * 发货组织
     */
    @TableField("delivery_org_id")
    private String deliveryOrgId;

    /**
     * 发货组织名
     */
    @TableField("delivery_org_name")
    private String deliveryOrgName;



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
     * 实际发货日期
     */
    @TableField("actual_delivery_date")
    private LocalDate actualDeliveryDate;

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
     * 客户id
     */
    @TableField("consumer_id ")
    private String consumerId;

    /**
     * 客户
     */
    @TableField("consumer_name")
    private String consumerName;

    /**
     * 收货人
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 联系电话
     */
    @TableField("tel_number")
    private String telNumber;

    /**
     * 联系地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 交货方式
     */
    @TableField("delivery_mode_dict")
    private String deliveryModeDict;

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
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String TYPE = "type";

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
