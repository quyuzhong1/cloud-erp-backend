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
 * 海外仓签收记录
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_warehouse_inbound_received")
public class OverseasWarehouseInboundReceivedEntity extends BaseEntity<OverseasWarehouseInboundReceivedEntity> {

    /**
    * 明细id
    */
    @TableField("detail_id")
    private String detailId;
    /**
    * 签收人
    */
    @TableField("receive_user")
    private String receiveUser;
    /**
    * 签收数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 签收时间
    */
    @TableField("receive_time")
    private OffsetDateTime receiveTime;


    public static final String DETAIL_ID = "detail_id";

    public static final String RECEIVE_USER = "receive_user";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String RECEIVE_TIME = "receive_time";

}