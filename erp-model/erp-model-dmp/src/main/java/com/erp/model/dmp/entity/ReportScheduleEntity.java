package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * 亚马逊报告计划表
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("report_schedule")
public class ReportScheduleEntity extends BaseEntity<ReportScheduleEntity> {

    /**
     * 报告计划ID
     */
    @TableField("report_schedule_id")
    private String reportScheduleId;
    /**
     * 市场IDS
     */
    @TableField("marketplace_ids")
    private String marketplaceIds;
    /**
     * OMS店铺ID
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 报告生成间隔时间
     * {@link com.erp.sdk.oms.amz.spapi.model.reports.CreateReportScheduleSpecification.PeriodEnum}
     */
    @TableField("period")
    private String period;
    /**
     * 首次创建下次创建报告的时间
     */
    @TableField("first_next_report_creation_time")
    private LocalDateTime firstNextReportCreationTime;
    /**
     * 报告类型
     */
    @TableField("report_type")
    private String reportType;
    /**
     * 订阅状态:not=未订阅,wait=待订阅,already=已订阅
     * {@link com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum}
     */
    @TableField("subscribed_status")
    private String subscribedStatus;
    /**
     * 取消状态:none=无(无需取消), wait=待取消, cancel=已取消(店铺取消授权)
     * {@link com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum}
     */
    @TableField("cancel_status")
    private String cancelStatus;
    /**
     * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)
     * {@link com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum}
     */
    @TableField("subscribed_type")
    private String subscribedType;


    public static final String REPORT_SCHEDULE_ID = "report_schedule_id";

    public static final String MARKETPLACE_IDS = "marketplace_ids";

    public static final String SHOP_ID = "shop_id";

    public static final String PERIOD = "period";

    public static final String FIRST_NEXT_REPORT_CREATION_TIME = "first_next_report_creation_time";

    public static final String REPORT_TYPE = "report_type";

    public static final String DISABLED = "disabled";

    /**
     * 初始化
     * CreateReportScheduleSpecification
     */
    public ReportScheduleEntity(String reportType, String marketplaceId, String shopId) {
        this.reportScheduleId = "";
        this.marketplaceIds = marketplaceId;
        this.shopId = shopId;
        // CreateReportScheduleSpecification.PeriodEnum
        if ("GET_FBA_INVENTORY_PLANNING_DATA".equalsIgnoreCase(reportType)){
            // AmazonReportRecordTypeEnum
            this.period = "PT15M";
        } else {
            this.period = "P1D";
        }
        this.firstNextReportCreationTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        this.reportType = reportType;
        this.subscribedStatus = ReportScheduleSubscribedStatusEnum.WAIT.getCode();
        this.cancelStatus = ReportScheduleCancelStatusEnum.NONE.getCode();
    }
}