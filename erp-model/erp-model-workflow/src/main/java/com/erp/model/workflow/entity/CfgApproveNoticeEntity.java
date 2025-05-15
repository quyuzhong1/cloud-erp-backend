package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * ERP审批同步-通知配置
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_approve_notice")
public class CfgApproveNoticeEntity extends BaseEntity<CfgApproveNoticeEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 通知类型：approve=审批通知,approveResult=审核结果通知,cc=抄送通知,timeoutWarning=超时预警通知,recall=撤回通知  枚举：CfgApproveNoticeNoticeTypeEnum
    */
    @TableField("notice_type")
    private String noticeType;
    /**
    * 角色：applicant=申请人,cc=抄送人,approver=审核人  枚举：CfgApproveNoticeRoleTypeEnum
    */
    @TableField("role_type")
    private String roleType;
    /**
    * 具体人员
    */
    @TableField("specific_person")
    private String specificPerson;
    /**
    * 是否启用
    */
    @TableField("enable_status")
    private Boolean enableStatus;


    public static final String MAIN_ID = "main_id";

    public static final String NOTICE_TYPE = "notice_type";

    public static final String ROLE_TYPE = "role_type";

    public static final String SPECIFIC_PERSON = "specific_person";

    public static final String ENABLE_STATUS = "enable_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
