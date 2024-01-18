package com.erp.model.dmp.entity;

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
 * 亚马逊报告类型配置
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_amz_report_type")
public class CfgAmzReportTypeEntity extends BaseEntity<CfgAmzReportTypeEntity> {

    /**
    * 报告类型
    */
    @TableField("report_type")
    private String reportType;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)
    */
    @TableField("subscribed_type")
    private String subscribedType;
    /**
    * 报告分组(会互斥的报告类型视为同组)
    */
    @TableField("report_group")
    private String reportGroup;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 是否全量更新: t=全量更新, f=增量更新
    */
    @TableField("is_full_update")
    private Boolean isFullUpdate;
    /**
    * 是否检查先前任务: t=是，f=否
    */
    @TableField("has_pre_task")
    private Boolean hasPreTask;


    public static final String REPORT_TYPE = "report_type";

    public static final String REMARK = "remark";

    public static final String SUBSCRIBED_TYPE = "subscribed_type";

    public static final String REPORT_GROUP = "report_group";

    public static final String DISABLED = "disabled";

    public static final String IS_FULL_UPDATE = "is_full_update";

    public static final String HAS_PRE_TASK = "has_pre_task";

}