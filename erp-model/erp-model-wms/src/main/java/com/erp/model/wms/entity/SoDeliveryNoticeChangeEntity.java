package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 发货通知变更单
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_delivery_notice_change")
public class SoDeliveryNoticeChangeEntity extends BaseEntity<SoDeliveryNoticeChangeEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态 
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源Id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 销售订单编号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 客户表id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 客户名称
    */
    @TableField("customer_name")
    private String customerName;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人名称
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 变更原因
    */
    @TableField("change_reason")
    private String changeReason;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_ID = "source_id";

    public static final String SO_CODE = "so_code";

    public static final String SO_ID = "so_id";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String CHANGE_REASON = "change_reason";

    @Override
    public Serializable pkVal() {
        return null;
    }

}