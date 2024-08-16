package com.erp.server.dmp.convert;

import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.server.dmp.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;


/**
 * <p>
 * 亚马逊报告映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface DmpReportConverter {
    DmpReportConverter INSTANCE = Mappers.getMapper(DmpReportConverter.class);


    @Mappings({
            @Mapping(target = "mainId", source = "reportScheduleEntity.id"),
            @Mapping(target = "shopId", source = "reportScheduleEntity.shopId"),
            @Mapping(target = "createdTime", expression = "java(java.time.LocalDateTime.now().toString())"),
            @Mapping(target = "reportDocumentId", source = "reportDocument.reportDocumentId"),
            @Mapping(target = "reportDocumentUrl", source = "reportDocument.url"),
            @Mapping(target = "reportHandleStatus", constant = "0"),
            @Mapping(target = "reportCancelStatus", constant = "0"),
            @Mapping(target = "marketplaceIds", source = "report.marketplaceIds"),
            @Mapping(target = "reportType", source = "report.reportType"),
            @Mapping(target = "reportId", source = "report.reportId"),
            @Mapping(target = "reportScheduleId", source = "report.reportScheduleId"),
            @Mapping(target = "dataStartTime", expression = "java(report.getDataStartTime().toString())"),
            @Mapping(target = "dataEndTime", expression = "java(report.getDataEndTime().toString())"),
            @Mapping(target = "processingStartTime", expression = "java(report.getProcessingStartTime().toString())"),
            @Mapping(target = "processingEndTime", expression = "java(report.getProcessingEndTime().toString())"),
            @Mapping(target = "processingStatus", expression = "java(report.getProcessingStatus().getValue())"),
    })
    ReportInfoMongoDTO newReportInfoMongoDTO(Report report, ReportDocument reportDocument, AmzReportScheduleEntity reportScheduleEntity);


    @Mappings({
            @Mapping(target = "reportDocumentId", expression = "java(null == reportDocument ? mongoDTO.getReportDocumentId() : reportDocument.getReportDocumentId())"),
            @Mapping(target = "reportDocumentUrl", expression = "java(null == reportDocument ? mongoDTO.getReportDocumentUrl() : reportDocument.getUrl())"),
            @Mapping(target = "reportHandleStatus", constant = "0"),
            @Mapping(target = "reportCancelStatus", constant = "0"),
            @Mapping(target = "marketplaceIds", source = "report.marketplaceIds"),
            @Mapping(target = "reportType", source = "report.reportType"),
            @Mapping(target = "reportScheduleId", source = "report.reportScheduleId"),
            @Mapping(target = "reportId", source = "report.reportId"),
            @Mapping(target = "reportCreatedTime", expression = "java(report.getCreatedTime().toString())"),
            @Mapping(target = "createdTime", source = "mongoDTO.createdTime"),
            @Mapping(target = "shopId", source = "mongoDTO.shopId"),
            @Mapping(target = "dataStartTime", expression = "java(report.getDataStartTime().toString())"),
            @Mapping(target = "dataEndTime", expression = "java(report.getDataEndTime().toString())"),
            @Mapping(target = "processingStartTime", expression = "java(report.getProcessingStartTime().toString())"),
            @Mapping(target = "processingEndTime", expression = "java(report.getProcessingEndTime().toString())"),
            @Mapping(target = "processingStatus", expression = "java(report.getProcessingStatus().getValue())"),
            @Mapping(target = "downloadStatus", constant = "1"),
    })
    ReportInfoMongoDTO updateReportInfoMongoDTO(ReportInfoMongoDTO mongoDTO, ReportDocument reportDocument, Report report);


    @Mappings(value = {
            @Mapping(target = "id", expression = "java(null)"),
            @Mapping(target = "createUserId", constant = ""),
            @Mapping(target = "createUserName", constant = "system"),
            @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "updateUserId", constant = ""),
            @Mapping(target = "updateUserName", constant = "system"),
            @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "version", constant = "0"),
            @Mapping(target = "isDeleted", constant = "false"),
            @Mapping(target = "mainId", source = "entity.id"),
            @Mapping(target = "shopId", source = "entity.shopId"),
            @Mapping(target = "reportType", source = "entity.reportType"),
            @Mapping(target = "reportTypeName", source = "entity.reportTypeName"),
            @Mapping(target = "marketplaceIds", source = "entity.marketplaceIds"),
            @Mapping(target = "reqDataStartTime", source = "reqDataStartTime"),
            @Mapping(target = "reqDataEndTime", source = "reqDataEndTime"),
            @Mapping(target = "reportId", constant = ""),
            @Mapping(target = "status", constant = "created"),
            @Mapping(target = "statusDesc", constant = "待请求/创建报表(第一步)"),
            @Mapping(target = "groupId", source = "groupId"),
    })
    AmzReportTaskEntity initScheduleEntityToTask(AmzReportScheduleEntity entity, String reqDataStartTime, String reqDataEndTime, String groupId);


    @Mappings({
            @Mapping(target = "id", expression = "java(null)"),
            @Mapping(target = "createUserId", constant = ""),
            @Mapping(target = "createUserName", constant = "system"),
            @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "updateUserId", constant = ""),
            @Mapping(target = "updateUserName", constant = "system"),
            @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "version", constant = "0"),
            @Mapping(target = "isDeleted", constant = "false"),
            @Mapping(target = "mainId", source = "taskEntity.id"),
            @Mapping(target = "shopId", source = "taskEntity.shopId"),
            @Mapping(target = "reportDocumentId", source = "report.reportDocumentId"),
            @Mapping(target = "reportUrl", constant = ""),
            @Mapping(target = "handleStatus", constant = "0"),
            @Mapping(target = "cancelStatus", constant = "0"),
            @Mapping(target = "marketplaceIds", source = "taskEntity.marketplaceIds"),
            @Mapping(target = "reportType", source = "report.reportType"),
            @Mapping(target = "reportId", source = "report.reportId"),
            @Mapping(target = "createdMethod", source = "createdMethod"),
            @Mapping(target = "reportScheduleId", expression = "java(null != report.getReportScheduleId() ? report.getReportScheduleId() : \"\")"),
            @Mapping(target = "dataStartTime", expression = "java(null != report.getDataStartTime() ? report.getDataStartTime().toString() : \"\")"),
            @Mapping(target = "dataEndTime", expression = "java(null != report.getDataEndTime() ? report.getDataEndTime().toString() : \"\" )"),
            @Mapping(target = "processStartTime", expression = "java(null != report.getProcessingStartTime() ? report.getProcessingStartTime().toString() : \"\")"),
            @Mapping(target = "processEndTime", expression = "java(null != report.getProcessingEndTime() ? report.getProcessingEndTime().toString() : \"\")"),
            @Mapping(target = "processingStatus", expression = "java(report.getProcessingStatus().getValue())"),
    })
    DmpAmzReportInfoEntity newReportInfoEntity(Report report, AmzReportTaskEntity taskEntity, String createdMethod);


    @Mappings({
            @Mapping(target = "id", expression = "java(null)"),
            @Mapping(target = "createUserId", constant = ""),
            @Mapping(target = "createUserName", constant = "system"),
            @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "updateUserId", constant = ""),
            @Mapping(target = "updateUserName", constant = "system"),
            @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "version", constant = "0"),
            @Mapping(target = "isDeleted", constant = "false"),
            @Mapping(target = "mainId", source = "entity.id"),
            @Mapping(target = "shopId", source = "entity.shopId"),
            @Mapping(target = "reportType", source = "entity.reportType"),
            @Mapping(target = "reportTypeName", source = "entity.reportTypeName"),
            @Mapping(target = "marketplaceIds", source = "entity.marketplaceIds"),
            @Mapping(target = "reqDataStartTime", source = "reqDataStartTime"),
            @Mapping(target = "reqDataEndTime", source = "reqDataEndTime"),
            @Mapping(target = "reportId", source = "reqDataEndTime"),
            @Mapping(target = "status", constant = "direct_query"),
            @Mapping(target = "statusDesc", constant = "直接获取报表(第一步/第二步)"),
            @Mapping(target = "groupId", source= "groupId"),
    })
    AmzReportTaskEntity initDirectQueryTask(AmzReportScheduleEntity entity, String groupId, String reqDataStartTime, String reqDataEndTime);
}
