package com.erp.sdk.oms.amz.spapi.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.dto.OrderMongoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain = true)
@NoArgsConstructor
public class ReportInfoMongoDTO {

    @Panno(findType = PannoEnum.EQ,field = "_id")
    private String id;

    @Panno(findType = PannoEnum.IN, field = "marketplaceIds")
    private List<String> marketplaceIds;

    @Panno(findType = PannoEnum.EQ, field = "reportId")
    private String reportId;

    @Panno(findType = PannoEnum.EQ, field = "reportType")
    private String reportType;

    @Panno(findType = PannoEnum.EQ, field = "dataStartTime")
    private String dataStartTime;

    @Panno(findType = PannoEnum.EQ, field = "dataEndTime")
    private String dataEndTime;

    /**
     * 亚马逊报告计划ID
     */
    @Panno(findType = PannoEnum.EQ, field = "reportScheduleId")
    private String reportScheduleId;

    /**
     * 记录创建时间
     */
    @Panno(findType = PannoEnum.EQ, field = "createdTime")
    private String createdTime;

    @Panno(findType = PannoEnum.EQ, field = "processingStatus")
    private String processingStatus;

    @Panno(findType = PannoEnum.EQ, field = "processingStartTime")
    private String processingStartTime;

    @Panno(findType = PannoEnum.EQ, field = "processingEndTime")
    private String processingEndTime;

    @Panno(findType = PannoEnum.EQ, field = "reportDocumentId")
    private String reportDocumentId;

    @Panno(findType = PannoEnum.EQ, field = "reportDocumentUrl")
    private String reportDocumentUrl;

    /**
     * 报告处理状态: 0=待处理，1=已处理
     */
    @Panno(findType = PannoEnum.EQ, field = "reportHandleStatus")
    private Integer reportHandleStatus;

    /**
     * 报告取消状态: 0=为取消，1=已取消
     */
    @Panno(findType = PannoEnum.EQ, field = "reportCancelStatus")
    private Integer reportCancelStatus;

    /**
     * 主表report_schedule_id
     */
    @Panno(findType = PannoEnum.EQ, field = "mainId")
    private String mainId;

    /**
     * 店铺ID
     */
    @Panno(findType = PannoEnum.EQ, field = "shopId")
    private String shopId;

    /**
     * 数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadStatus")
    private Integer downloadStatus;

    /**
     * 报告创建时间
     */
    @Panno(findType = PannoEnum.EQ, field = "reportCreatedTime")
    private String reportCreatedTime;

    /**
     * 报告来源
     * 0 ERP系统请求亚马逊创建
     * 1 请求亚马逊报告列表获取
     */
    @Panno(findType = PannoEnum.EQ,field = "reportSourceType")
    private Integer reportSourceType;

    public ReportInfoMongoDTO(String reportId) {
        this.reportId = reportId;
    }

    public static ReportInfoMongoDTO getByNotCheckDownload() {
        ReportInfoMongoDTO mongoDTO = new ReportInfoMongoDTO();
        mongoDTO.setDownloadStatus(0);
        mongoDTO.setProcessingStatus(null);
        return mongoDTO;
    }

    public static ReportInfoMongoDTO getReportId(String reportId) {
        ReportInfoMongoDTO reportMongoDTO = new ReportInfoMongoDTO();
        reportMongoDTO.setReportId(reportId);
        return reportMongoDTO;
    }

    public static ReportInfoMongoDTO getId(String id) {
        ReportInfoMongoDTO reportMongoDTO = new ReportInfoMongoDTO();
        reportMongoDTO.setId(id);
        return reportMongoDTO;
    }

    public static ReportInfoMongoDTO reportHandleStatus(Integer reportHandleStatus) {
        ReportInfoMongoDTO reportMongoDTO = new ReportInfoMongoDTO();
        reportMongoDTO.setReportHandleStatus(reportHandleStatus);
        return reportMongoDTO;
    }

    public static ReportInfoMongoDTO reportHandleStatusAndDone() {
        ReportInfoMongoDTO mongoDTO = new ReportInfoMongoDTO();
        mongoDTO.setReportHandleStatus(0);
        mongoDTO.setProcessingStatus("DONE");
        return mongoDTO;
    }

}
