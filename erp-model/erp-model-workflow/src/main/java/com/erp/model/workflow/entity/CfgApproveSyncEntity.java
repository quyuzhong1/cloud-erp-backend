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
 * ERP审批同步配置
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_approve_sync")
public class CfgApproveSyncEntity extends BaseEntity<CfgApproveSyncEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 来源类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * SDC回调地址
    */
    @TableField("webhook_url")
    private String webhookUrl;
    /**
    * 审批分组dict_basic表approveGroup
    */
    @TableField("approve_group")
    private String approveGroup;
    /**
    * 启用状态
    */
    @TableField("enable_status")
    private Boolean enableStatus;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 审批名称(标题)
    */
    @TableField("title")
    private String title;
    /**
    * 可见范围dict_basic表viewerType：allUsers=所有用户,specificDepartments=指定部门,specificUsers=指定用户,none=不可见  枚举：CfgApproveSyncViewerTypeEnum
    */
    @TableField("viewer_type")
    private String viewerType;
    /**
    * 可见集合
    */
    @TableField("viewer")
    private String viewer;
    /**
    * 同步平台：feishu=飞书,dd=钉钉,qw=企业微信  枚举：CfgApproveSyncSyncPlatformEnum
    */
    @TableField("sync_platform")
    private String syncPlatform;
    /**
    * 飞书审批定义
    */
    @TableField("approval_code")
    private String approvalCode;


    public static final String CODE = "code";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String WEBHOOK_URL = "webhook_url";

    public static final String APPROVE_GROUP = "approve_group";

    public static final String ENABLE_STATUS = "enable_status";

    public static final String REMARK = "remark";

    public static final String TITLE = "title";

    public static final String VIEWER_TYPE = "viewer_type";

    public static final String VIEWER = "viewer";

    public static final String SYNC_PLATFORM = "sync_platform";

    public static final String APPROVAL_CODE = "approval_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
