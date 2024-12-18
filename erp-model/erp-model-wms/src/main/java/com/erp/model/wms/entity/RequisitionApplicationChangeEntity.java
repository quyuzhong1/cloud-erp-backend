package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 要货申请变更单
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("requisition_application_change")
public class RequisitionApplicationChangeEntity extends BaseEntity<RequisitionApplicationChangeEntity> {

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
    * 业务单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 业务id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
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
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 明细
     */
    @TableField(exist = false)
    private List<RequisitionApplicationChangeDetailEntity> details;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_ID = "source_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_ID = "business_id";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}