package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


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
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime receiveTime;
    /**
     * 数据来源
     * SignSourceTypeEnum
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 流水ID
     */
    @TableField("flow_id")
    private String flowId;
    /**
     * 是否不良品 true 是 false 否
     */
    @TableField("defective_product_flag")
    private Boolean defectiveProductFlag;


    public static final String DETAIL_ID = "detail_id";

    public static final String RECEIVE_USER = "receive_user";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String RECEIVE_TIME = "receive_time";

    public static final String DEFECTIVE_PRODUCT_FLAG = "defective_product_flag";

}