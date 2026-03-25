package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 质检申请单主表
 * </p>
 *
 * @author will
 * @since 2026-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_application")
public class QcApplicationEntity extends BaseEntity<QcApplicationEntity> {

    /**
    * 质检申请单号
    */
    @TableField("code")
    private String code;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编码
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 单据状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审核完成人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 审核完成时间
    */
    @TableField("approve_time")
    private LocalDate approveTime;
    /**
    * 质检类型
    */
    @TableField("qc_type")
    private String qcType;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 期望质检日期
    */
    @TableField("plan_qc_date")
    private LocalDate planQcDate;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String TYPE = "type";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String PLAN_DATE = "plan_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}