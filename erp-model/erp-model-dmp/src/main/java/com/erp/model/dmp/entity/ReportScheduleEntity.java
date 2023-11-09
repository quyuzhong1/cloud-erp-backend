package com.erp.model.dmp.entity;

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
 * 亚马逊报告计划表
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
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
    * 订阅状态:订阅状态:not=未订阅,wait=待订阅,already=已订阅
    */
    @TableField("subscribed_status")
    private String subscribedStatus;


    public static final String REPORT_SCHEDULE_ID = "report_schedule_id";

    public static final String MARKETPLACE_IDS = "marketplace_ids";

    public static final String SHOP_ID = "shop_id";

    public static final String PERIOD = "period";

    public static final String FIRST_NEXT_REPORT_CREATION_TIME = "first_next_report_creation_time";

    public static final String REPORT_TYPE = "report_type";

    public static final String DISABLED = "disabled";

}