package com.erp.server.dmp.convert;

import com.erp.model.dmp.entity.ReportScheduleEntity;
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
    ReportInfoMongoDTO newReportInfoMongoDTO(Report report, ReportDocument reportDocument, ReportScheduleEntity reportScheduleEntity);


    @Mappings({
            @Mapping(target = "reportDocumentId", expression = "java(null == reportDocument ? mongoDTO.getReportDocumentId() : reportDocument.getReportDocumentId())"),
            @Mapping(target = "reportDocumentUrl", expression = "java(null == reportDocument ? mongoDTO.getReportDocumentUrl() : reportDocument.getUrl())"),
            @Mapping(target = "reportHandleStatus", constant = "0"),
            @Mapping(target = "reportCancelStatus", constant = "0"),
            @Mapping(target = "marketplaceIds", source = "report.marketplaceIds"),
            @Mapping(target = "reportType", source = "report.reportType"),
            @Mapping(target = "reportScheduleId", source = "report.reportScheduleId"),
            @Mapping(target = "reportId", source = "report.reportId"),
            @Mapping(target = "createdTime", source = "mongoDTO.createdTime"),
            @Mapping(target = "shopId", source = "mongoDTO.shopId"),
            @Mapping(target = "dataStartTime", expression = "java(report.getDataStartTime().toString())"),
            @Mapping(target = "dataEndTime", expression = "java(report.getDataEndTime().toString())"),
            @Mapping(target = "processingStartTime", expression = "java(report.getProcessingStartTime().toString())"),
            @Mapping(target = "processingEndTime", expression = "java(report.getProcessingEndTime().toString())"),
            @Mapping(target = "processingStatus", expression = "java(report.getProcessingStatus().getValue())"),
    })
    ReportInfoMongoDTO updateReportInfoMongoDTO(ReportInfoMongoDTO mongoDTO, ReportDocument reportDocument, Report report);
}
