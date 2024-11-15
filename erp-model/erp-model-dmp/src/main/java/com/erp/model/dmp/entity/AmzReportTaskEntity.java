package com.erp.model.dmp.entity;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 亚马逊报告请求记录
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("amz_report_task")
public class AmzReportTaskEntity extends BaseEntity<AmzReportTaskEntity> {

    /**
     * (amz_report_schedule)报告计划任务ID
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 店铺ID
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 报告类型
     */
    @TableField("report_type")
    private String reportType;
    /**
     * 报告类型名称
     */
    @TableField("report_type_name")
    private String reportTypeName;
    /**
     * 报告市场Id列表
     */
    @TableField("marketplace_ids")
    private String marketplaceIds;
    /**
     * 请求参数数据开始时间
     */
    @TableField("req_data_start_time")
    private String reqDataStartTime;
    /**
     * 请求参数数据结束时间
     */
    @TableField("req_data_end_time")
    private String reqDataEndTime;
    /**
     * 报告请求时间(第一步)
     */
    @TableField("report_created_time")
    private LocalDateTime reportCreatedTime;
    /**
     * 报告查询列表时间(第二步)
     */
    @TableField("report_query_time")
    private LocalDateTime reportQueryTime;
    /**
     * 报告下载数据时间(第三步)
     */
    @TableField("report_download_time")
    private LocalDateTime reportDownloadTime;
    /**
     * 报告解析时间(第四步)
     */
    @TableField("report_parse_time")
    private LocalDateTime reportParseTime;
    /**
     * 亚马逊生成报告完成时间
     */
    @TableField("completed_time")
    private LocalDateTime completedTime;
    /**
     * 生成的报告
     */
    @TableField("report_id")
    private String reportId;
    /**
     * 处理状态;created-待请求/创建报表(第一步),query-待获取列表(第二步),download-待下载数据(第三步),parse-待解析(第四步),finish-已完成
     */
    @TableField("status")
    private String status;
    /**
     * 状态处理
     */
    @TableField("status_desc")
    private String statusDesc;
    /**
     * 最近错误消息
     */
    @TableField("error_msg")
    private String errorMsg;
    /**
     * 请求创建报告重试次数
     */
    @TableField("created_retry_count")
    private Integer createdRetryCount;
    /**
     * 查询报告重试次数
     */
    @TableField("query_retry_count")
    private Integer queryRetryCount;
    /**
     * 下载报告重试次数
     */
    @TableField("download_retry_count")
    private Integer downloadRetryCount;
    /**
     * 解析报告重试次数
     */
    @TableField("parse_retry_count")
    private Integer parseRetryCount;
    /**
     * 请求的分组ID
     * amazon:sellerId:端口区域
     */
    @TableField("group_id")
    private String groupId;
    /**
     * 已解析的报告行数索引
     */
    @TableField("parse_row_index")
    private Integer parseRowIndex;


    public static final String MAIN_ID = "main_id";

    public static final String SHOP_ID = "shop_id";

    public static final String REPORT_TYPE = "report_type";

    public static final String REPORT_TYPE_NAME = "report_type_name";

    public static final String MARKETPLACE_IDS = "marketplace_ids";

    public static final String REQ_DATA_START_TIME = "req_data_start_time";

    public static final String REQ_DATA_END_TIME = "req_data_end_time";

    public static final String REPORT_CREATED_TIME = "report_created_time";

    public static final String REPORT_QUERY_TIME = "report_query_time";

    public static final String REPORT_DOWNLOAD_TIME = "report_download_time";

    public static final String REPORT_PARSE_TIME = "report_parse_time";

    public static final String COMPLETED_TIME = "completed_time";

    public static final String REPORT_ID = "report_id";

    public static final String STATUS = "status";

    public static final String STATUS_DESC = "status_desc";

    public static final String ERROR_MSG = "error_msg";

    public static final String CREATED_RETRY_COUNT = "created_retry_count";

    public static final String QUERY_RETRY_COUNT = "query_retry_count";

    public static final String DOWNLOAD_RETRY_COUNT = "download_retry_count";

    public static final String PARSE_RETRY_COUNT = "parse_retry_count";

    public String getFirstMarketplace() {
        if (StringUtils.isNotBlank(this.marketplaceIds)){
            return this.marketplaceIds.split(",")[0];
        }
        return "";
    }

    /**
     * 获取亚马逊账号代号
     */
    public String parsePlatformShopCode() {
       // Amazon:A7XZMJAHE7S5Z:US_EAST_1取出A7XZMJAHE7S5Z
        return StringUtils.substringBetween(this.groupId, ":", ":");
    }

    public Integer checkAndGetQueryRetryCount() {
        return null == this.getQueryRetryCount() ? 0 : this.getCreatedRetryCount();
    }
}