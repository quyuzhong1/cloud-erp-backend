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
 * 亚马逊报告请求记录
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("amz_report_info")
public class AmzReportInfoEntity extends BaseEntity<AmzReportInfoEntity> {

    /**
    * 市场报告ID列表
    */
    @TableField("marketplace_ids")
    private String marketplaceIds;
    /**
    * 店铺ID
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * report_request_task请求记录ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 报告类型
    */
    @TableField("report_type")
    private String reportType;
    /**
    * 报告类型
    */
    @TableField("report_type_name")
    private String reportTypeName;
    /**
    * 数据开始时间
    */
    @TableField("data_start_time")
    private LocalDateTime dataStartTime;
    /**
    * 数据结束时间
    */
    @TableField("data_end_time")
    private LocalDateTime dataEndTime;
    /**
    * 亚马逊报告创建时间
    */
    @TableField("report_created_time")
    private LocalDateTime reportCreatedTime;
    /**
    * 亚马逊报告计划ID
    */
    @TableField("report_schedule_id")
    private String reportScheduleId;
    /**
    * 报告处理开始时间
    */
    @TableField("process_start_time")
    private LocalDateTime processStartTime;
    /**
    * 报告处理结束时间
    */
    @TableField("process_end_time")
    private LocalDateTime processEndTime;
    /**
    * 报告文档ID
    */
    @TableField("report_document_id")
    private String reportDocumentId;
    /**
    * 报告文档下载路径
    */
    @TableField("amz_report_document_url")
    private String amzReportDocumentUrl;
    /**
    * 报告文件的存储路径
    */
    @TableField("local_file_path")
    private String localFilePath;
    /**
    * 亚马逊报告ID
    */
    @TableField("report_id")
    private String reportId;
    /**
    * 亚马逊报告处理状态：CANCELLED=取消，DONE=已完成，PROCESS=处理中，FATAL=失败
    */
    @TableField("processing_status")
    private String processingStatus;
    /**
    * 取消状态
    */
    @TableField("cancel_status")
    private String cancelStatus;
    /**
    * 取消时间
    */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;
    /**
    * 处理状态
    */
    @TableField("handle_status")
    private String handleStatus;
    /**
    * 处理时间
    */
    @TableField("handle_time")
    private LocalDateTime handleTime;
    /**
    * 创建方式: system=ERP系统自动请求创建，query=查询亚马逊系统创建，manual=ERP系统人工请求创建
    */
    @TableField("created_method")
    private String createdMethod;


    public static final String MARKETPLACE_IDS = "marketplace_ids";

    public static final String SHOP_ID = "shop_id";

    public static final String MAIN_ID = "main_id";

    public static final String REPORT_TYPE = "report_type";

    public static final String REPORT_TYPE_NAME = "report_type_name";

    public static final String DATA_START_TIME = "data_start_time";

    public static final String DATA_END_TIME = "data_end_time";

    public static final String REPORT_CREATED_TIME = "report_created_time";

    public static final String REPORT_SCHEDULE_ID = "report_schedule_id";

    public static final String PROCESS_START_TIME = "process_start_time";

    public static final String PROCESS_END_TIME = "process_end_time";

    public static final String REPORT_DOCUMENT_ID = "report_document_id";

    public static final String AMZ_REPORT_DOCUMENT_URL = "amz_report_document_url";

    public static final String LOCAL_FILE_PATH = "local_file_path";

    public static final String REPORT_ID = "report_id";

    public static final String PROCESSING_STATUS = "processing_status";

    public static final String CANCEL_STATUS = "cancel_status";

    public static final String CANCEL_TIME = "cancel_time";

    public static final String HANDLE_STATUS = "handle_status";

    public static final String HANDLE_TIME = "handle_time";

    public static final String CREATED_METHOD = "created_method";

}