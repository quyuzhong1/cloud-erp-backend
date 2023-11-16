package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 海外仓入库单
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_warehouse_inbound")
public class OverseasWarehouseInboundEntity extends BaseEntity<OverseasWarehouseInboundEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 平台类型: goodcang=谷仓，iml=艾姆勒
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 入库类型
    */
    @TableField("instock_type")
    private String instockType;
    /**
    * 入库状态
    */
    @TableField("instock_status")
    private String instockStatus;
    /**
    * 发货仓名称
    */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;
    /**
    * 发货仓ID
    */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;
    /**
    * 中转仓名称
    */
    @TableField("transfer_warehouse_name")
    private String transferWarehouseName;
    /**
    * 中转仓ID
    */
    @TableField("transfer_warehouse_id")
    private String transferWarehouseId;
    /**
    * 目的仓名称
    */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
    /**
    * 目的仓ID
    */
    @TableField("to_warehouse_id")
    private String toWarehouseId;
    /**
    * 物流方式
    */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 最新签收时间
    */
    @TableField("receive_time")
    private OffsetDateTime receiveTime;
    /**
    * 预计到达时间
    */
    @TableField("estimated_arrival_date")
    private OffsetDateTime estimatedArrivalDate;
    /**
    * 手动完结原因
    */
    @TableField("finish_reason")
    private String finishReason;
    /**
    * 完结状态: not=未完结, auto=自动完结，manual=手动完结
    */
    @TableField("finish_status")
    private String finishStatus;
    /**
    * 第三方唯一编码
    */
    @TableField("overseas_warehouse_inbound_id")
    private String overseasWarehouseInboundId;


    public static final String CODE = "code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String INSTOCK_TYPE = "instock_type";

    public static final String INSTOCK_STATUS = "instock_status";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String TRANSFER_WAREHOUSE_NAME = "transfer_warehouse_name";

    public static final String TRANSFER_WAREHOUSE_ID = "transfer_warehouse_id";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String TO_WAREHOUSE_ID = "to_warehouse_id";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String REMARK = "remark";

    public static final String RECEIVE_TIME = "receive_time";

    public static final String ESTIMATED_ARRIVAL_DATE = "estimated_arrival_date";

    public static final String FINISH_REASON = "finish_reason";

    public static final String FINISH_STATUS = "finish_status";

    public static final String OVERSEAS_WAREHOUSE_INBOUND_ID = "overseas_warehouse_inbound_id";
}