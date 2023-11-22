package com.erp.server.dmp.service;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * <p>
 * 亚马逊报告处理 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
public interface ReportHandleService {

    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    Boolean pullShipment(DmpPullShipmentDTO dto);

    /**
     * 请求报告计划
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    void createReportSchedule(ReportScheduleEntity reportSchedule, OffsetDateTime roundedDateTime) throws Exception;

    /**
     * 请求创建报告
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    void createReport(ReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime) throws Exception;

    /**
     * 推送到处理器
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    void pullBusinessHandler(String shopId, String reportId, List<? extends ReportSuperMongoDTO> mongoDTOSList);

    /**
     * 亚马逊报告通知处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void handlerNotifications(SQSTextMessage textMessage) throws Exception;

    /**
     * 保存亚马逊报告信息并处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void saveMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, ReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO) throws IOException;

    /**
     * 更新亚马逊报告信息并处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void updateMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, ReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO) throws IOException;


    /**
     * 库存管理报告保存或更新
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void saveOrUpdateAllReportFbaMyiAllInventory(ReportInfoMongoDTO mongoDTO, List<ReportFbaMyiAllInventoryMongoDTO> fbaMyiAllInventoryMongoDTOList);

    /**
     * 库存预留报告保存或更新
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void saveOrUpdateAllReportReserved(ReportInfoMongoDTO mongoDTO, List<ReportReservedMongoDTO> reportReservedMongoDTOList);

    /**
     * 库存状态报告保存或更新
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void saveOrUpdateAllReportFbaInventoryPlanning(ReportInfoMongoDTO mongoDTO, List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList);
}
