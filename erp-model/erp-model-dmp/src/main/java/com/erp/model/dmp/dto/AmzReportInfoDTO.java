package com.erp.model.dmp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 亚马逊报告请求记录请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@NoArgsConstructor
public class AmzReportInfoDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 市场报告ID列表
        */
        private String marketplaceIds;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * report_request_task请求记录ID
        */
        private String mainId;

        /**
        * 报告类型
        */
        private String reportType;

        /**
        * 报告类型
        */
        private String reportTypeName;

        /**
        * 数据开始时间
        */
        private LocalDateTime dataStartTime;

        /**
        * 数据结束时间
        */
        private LocalDateTime dataEndTime;

        /**
        * 亚马逊报告创建时间
        */
        private LocalDateTime reportCreatedTime;

        /**
        * 亚马逊报告计划ID
        */
        private String reportScheduleId;

        /**
        * 报告处理开始时间
        */
        private LocalDateTime processStartTime;

        /**
        * 报告处理结束时间
        */
        private LocalDateTime processEndTime;

        /**
        * 报告文档ID
        */
        private String reportDocumentId;

        /**
        * 报告文档下载路径
        */
        private String amzReportDocumentUrl;

        /**
        * 报告文件的存储路径
        */
        private String localFilePath;

        /**
        * 亚马逊报告ID
        */
        private String reportId;

        /**
        * 亚马逊报告处理状态：CANCELLED=取消，DONE=已完成，PROCESS=处理中，FATAL=失败
        */
        private String processingStatus;

        /**
        * 取消状态
        */
        private String cancelStatus;

        /**
        * 取消时间
        */
        private LocalDateTime cancelTime;

        /**
        * 处理状态
        */
        private String handleStatus;

        /**
        * 处理时间
        */
        private LocalDateTime handleTime;

        /**
        * 创建方式: system=ERP系统自动请求创建，query=查询亚马逊系统创建，manual=ERP系统人工请求创建
        */
        private String createdMethod;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 市场报告ID列表
        */
        @NotBlank(message = "市场报告ID列表不能为空")
        @Size(max = 100,message = "市场报告ID列表最大长度不能超过100位")
        private String marketplaceIds;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String shopId;

        /**
        * report_request_task请求记录ID
        */
        @NotBlank(message = "report_request_task请求记录ID不能为空")
        @Size(max = 19,message = "report_request_task请求记录ID最大长度不能超过19位")
        private String mainId;

        /**
        * 报告类型
        */
        @NotBlank(message = "报告类型不能为空")
        @Size(max = 100,message = "报告类型最大长度不能超过100位")
        private String reportType;

        /**
        * 报告类型
        */
        @NotBlank(message = "报告类型不能为空")
        @Size(max = 100,message = "报告类型最大长度不能超过100位")
        private String reportTypeName;

        /**
        * 数据开始时间
        */
        private LocalDateTime dataStartTime;

        /**
        * 数据结束时间
        */
        private LocalDateTime dataEndTime;

        /**
        * 亚马逊报告创建时间
        */
        private LocalDateTime reportCreatedTime;

        /**
        * 亚马逊报告计划ID
        */
        @NotBlank(message = "亚马逊报告计划ID不能为空")
        @Size(max = 100,message = "亚马逊报告计划ID最大长度不能超过100位")
        private String reportScheduleId;

        /**
        * 报告处理开始时间
        */
        private LocalDateTime processStartTime;

        /**
        * 报告处理结束时间
        */
        private LocalDateTime processEndTime;

        /**
        * 报告文档ID
        */
        @NotBlank(message = "报告文档ID不能为空")
        @Size(max = 255,message = "报告文档ID最大长度不能超过255位")
        private String reportDocumentId;

        /**
        * 报告文档下载路径
        */
        @NotBlank(message = "报告文档下载路径不能为空")
        @Size(max = 255,message = "报告文档下载路径最大长度不能超过255位")
        private String amzReportDocumentUrl;

        /**
        * 报告文件的存储路径
        */
        @NotBlank(message = "报告文件的存储路径不能为空")
        @Size(max = 255,message = "报告文件的存储路径最大长度不能超过255位")
        private String localFilePath;

        /**
        * 亚马逊报告ID
        */
        @NotBlank(message = "亚马逊报告ID不能为空")
        @Size(max = 100,message = "亚马逊报告ID最大长度不能超过100位")
        private String reportId;

        /**
        * 亚马逊报告处理状态：CANCELLED=取消，DONE=已完成，PROCESS=处理中，FATAL=失败
        */
        @NotBlank(message = "亚马逊报告处理状态：CANCELLED=取消，DONE=已完成，PROCESS=处理中，FATAL=失败不能为空")
        @Size(max = 32,message = "亚马逊报告处理状态：CANCELLED=取消，DONE=已完成，PROCESS=处理中，FATAL=失败最大长度不能超过32位")
        private String processingStatus;

        /**
        * 取消状态
        */
        @NotBlank(message = "取消状态不能为空")
        @Size(max = 32,message = "取消状态最大长度不能超过32位")
        private String cancelStatus;

        /**
        * 取消时间
        */
        private LocalDateTime cancelTime;

        /**
        * 处理状态
        */
        @NotBlank(message = "处理状态不能为空")
        @Size(max = 32,message = "处理状态最大长度不能超过32位")
        private String handleStatus;

        /**
        * 处理时间
        */
        private LocalDateTime handleTime;

        /**
        * 创建方式: system=ERP系统自动请求创建，query=查询亚马逊系统创建，manual=ERP系统人工请求创建
        */
        @NotBlank(message = "创建方式: system=ERP系统自动请求创建，query=查询亚马逊系统创建，manual=ERP系统人工请求创建不能为空")
        @Size(max = 32,message = "创建方式: system=ERP系统自动请求创建，query=查询亚马逊系统创建，manual=ERP系统人工请求创建最大长度不能超过32位")
        private String createdMethod;


    }


}