package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 委托审批
 * </p>
 *
 * @author will
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("process_delegate")
public class ProcessDelegateEntity extends BaseEntity<ProcessDelegateEntity> {

    /**
    * 委托编号
    */
    @TableField("code")
    private String code;
    /**
    * 委托状态,ProcessDelegateStatusEnum枚举
    */
    @TableField("status")
    private String status;
    /**
    * 委托流程（单据类型）
    */
    @TableField("business_key")
    private String businessKey;
    /**
    * 发起人ID
    */
    @TableField("start_user_id")
    private String startUserId;
    /**
    * 委托人ID
    */
    @TableField("delegate_user_id")
    private String delegateUserId;
    /**
    * 委托生效时间
    */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
    /**
    * 委托失效时间
    */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    /**
    * 终止时间
    */
    @TableField("closed_time")
    private LocalDateTime closedTime;

    /**
     * 是否自动终止
     */
    @TableField("is_auto")
    private Boolean isAuto;

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String BUSINESS_KEY = "business_key";

    public static final String START_USER_ID = "start_user_id";

    public static final String DELEGATE_USER_ID = "delegate_user_id";

    public static final String EFFECTIVE_TIME = "effective_time";

    public static final String EXPIRE_TIME = "expire_time";

    public static final String CLOSED_TIME = "closed_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}