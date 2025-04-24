package com.erp.model.dmp.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


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
@NoArgsConstructor
@TableName(value = "cfg_amz_report_type", autoResultMap = true)
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
     * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob),query=查询最新
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
    /**
     * 报告生成间隔时间:PT5M，PT15M，PT30M，PT1H，PT2H，PT4H，PT8H，PT12H，P1D，P2D，P3D，PT84H，P7D，P14D，P15D，P18D，P30D，P1M
     * {@link com.erp.sdk.oms.amz.spapi.model.reports.CreateReportScheduleSpecification.PeriodEnum}
     */
    @TableField("period")
    private String period;
    /**
     * 请求创建报告队列延时等级
     */
    @TableField("created_delay_level")
    private Integer createdDelayLevel;
    /**
     * 查询报告队列延时等级
     */
    @TableField("query_delay_level")
    private Integer queryDelayLevel;
    /**
     * 下载报告队列延时等级
     */
    @TableField("download_delay_level")
    private Integer downloadDelayLevel;
    /**
     * 解析报告队列延时等级
     */
    @TableField("parse_delay_level")
    private Integer parseDelayLevel;
    /**
     * 直接查询报告队列延时等级
     */
    @TableField("direct_query_delay_level")
    private Integer directQueryDelayLevel;
    /**
     * 报告类型支持的国家/市场列表
     */
    @TableField(value = "country_list", typeHandler = JacksonTypeHandler.class)
    private List<String> countryList;
    /**
     * 解析的此报告类型每次保存的行数
     */
    @TableField("parse_row_count")
    private Integer parseRowCount;

    public static final String REPORT_TYPE = "report_type";

    public static final String REMARK = "remark";

    public static final String SUBSCRIBED_TYPE = "subscribed_type";

    public static final String REPORT_GROUP = "report_group";

    public static final String DISABLED = "disabled";

    public static final String IS_FULL_UPDATE = "is_full_update";

    public static final String HAS_PRE_TASK = "has_pre_task";

}