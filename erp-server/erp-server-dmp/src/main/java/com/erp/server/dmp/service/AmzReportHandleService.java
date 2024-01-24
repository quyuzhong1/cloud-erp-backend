package com.erp.server.dmp.service;

import cn.hutool.json.JSONObject;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 亚马逊报告处理 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
public interface AmzReportHandleService {

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
    void createReportSchedule(AmzReportScheduleEntity reportSchedule, OffsetDateTime roundedDateTime) throws Exception;

    /**
     * 亚马逊报告通知处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void handlerNotifications(JSONObject textMessage) throws Exception;

    /**
     * 保存亚马逊报告信息并处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void saveMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, AmzReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO, Map<String, String> columnMap) throws IOException;

    /**
     * 更新亚马逊报告信息并处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void updateMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, AmzReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO, Map<String, String> columnMap) throws IOException;


    /**
     * 查询CSV实体并下载
     *
     * @Author Jim
     * @since 2023-12-20
     **/

    public List<?> handleDownloadAndParse(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Map<String, String> columnMap) throws IOException ;

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

    /**
     * 通用处理
     * 处理和下载报告
     *
     * @Author Jim
     * @since 2023-12-04
     **/
    void handleReport(ReportsApi reportsApi, Report report, AmazonReportRecordTypeEnum recordTypeEnum, Map<String, String> columnMap) throws Exception;

    /**
     * 请求创建亚马逊报告
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    String createAmzReport(AmzReportTaskEntity taskEntity);

    /**
     * 查询亚马逊报告
     *
     * @Author Jim
     * @since 2024-01-20
     **/
    Report queryAmzReportInfo(AmzReportTaskEntity taskEntity);

    /**
     *
     * 获取报告文档
     *
     * @Author Jim
     * @since 2024-01-20
     **/
    ReportDocument queryAmzReportDocument(AmzReportInfoEntity reportInfoEntity, AmzReportTaskEntity entity);

}
