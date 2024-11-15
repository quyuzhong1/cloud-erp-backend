package com.erp.model.dmp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class AmzReportTaskDTO implements Serializable {




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
        * (amz_report_schedule)报告计划任务ID
        */
        private String mainId;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 报告类型
        */
        private String reportType;

        /**
        * 报告类型名称
        */
        private String reportTypeName;

        /**
        * 报告市场Id列表
        */
        private String marketplaceIds;

        /**
        * 请求参数数据开始时间
        */
        private LocalDateTime reqDataStartTime;

        /**
        * 请求参数数据结束时间
        */
        private LocalDateTime reqDataEndTime;

        /**
        * 报告请求时间(第一步)
        */
        private LocalDateTime reportCreatedTime;

        /**
        * 报告查询列表时间(第二步)
        */
        private LocalDateTime reportQueryTime;

        /**
        * 报告下载数据时间(第三步)
        */
        private LocalDateTime reportDownloadTime;

        /**
        * 报告解析时间(第四步)
        */
        private LocalDateTime reportParseTime;

        /**
        * 亚马逊生成报告完成时间
        */
        private LocalDateTime completedTime;

        /**
        * 生成的报告
        */
        private String reportId;

        /**
        * 处理状态;created-待请求/创建报表(第一步),query-待获取列表(第二步),download-待下载数据(第三步),parse-待解析(第四步),finish-已完成
        */
        private String status;

        /**
        * 状态处理
        */
        private String statusDesc;

        /**
        * 最近错误消息
        */
        private String errorMsg;

        /**
        * 请求创建报告重试次数
        */
        private Integer createdRetryCount;

        /**
        * 查询报告重试次数
        */
        private Integer queryRetryCount;

        /**
        * 下载报告重试次数
        */
        private Integer downloadRetryCount;

        /**
        * 解析报告重试次数
        */
        private Integer parseRetryCount;


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
        * (amz_report_schedule)报告计划任务ID
        */
        @NotBlank(message = "(amz_report_schedule)报告计划任务ID不能为空")
        @Size(max = 19,message = "(amz_report_schedule)报告计划任务ID最大长度不能超过19位")
        private String mainId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String shopId;

        /**
        * 报告类型
        */
        @NotBlank(message = "报告类型不能为空")
        @Size(max = 100,message = "报告类型最大长度不能超过100位")
        private String reportType;

        /**
        * 报告类型名称
        */
        @NotBlank(message = "报告类型名称不能为空")
        @Size(max = 100,message = "报告类型名称最大长度不能超过100位")
        private String reportTypeName;

        /**
        * 报告市场Id列表
        */
        @NotBlank(message = "报告市场Id列表不能为空")
        @Size(max = 100,message = "报告市场Id列表最大长度不能超过100位")
        private String marketplaceIds;

        /**
        * 请求参数数据开始时间
        */
        private LocalDateTime reqDataStartTime;

        /**
        * 请求参数数据结束时间
        */
        private LocalDateTime reqDataEndTime;

        /**
        * 报告请求时间(第一步)
        */
        private LocalDateTime reportCreatedTime;

        /**
        * 报告查询列表时间(第二步)
        */
        private LocalDateTime reportQueryTime;

        /**
        * 报告下载数据时间(第三步)
        */
        private LocalDateTime reportDownloadTime;

        /**
        * 报告解析时间(第四步)
        */
        private LocalDateTime reportParseTime;

        /**
        * 亚马逊生成报告完成时间
        */
        private LocalDateTime completedTime;

        /**
        * 生成的报告
        */
        @NotBlank(message = "生成的报告不能为空")
        @Size(max = 100,message = "生成的报告最大长度不能超过100位")
        private String reportId;

        /**
        * 处理状态;created-待请求/创建报表(第一步),query-待获取列表(第二步),download-待下载数据(第三步),parse-待解析(第四步),finish-已完成
        */
        @NotBlank(message = "处理状态;created不能为空")
        @Size(max = 32,message = "处理状态;created最大长度不能超过32位")
        private String status;

        /**
        * 状态处理
        */
        @NotBlank(message = "状态处理不能为空")
        @Size(max = 100,message = "状态处理最大长度不能超过100位")
        private String statusDesc;

        /**
        * 最近错误消息
        */
        @NotBlank(message = "最近错误消息不能为空")
        private String errorMsg;

        /**
        * 请求创建报告重试次数
        */
        @NotNull(message = "请求创建报告重试次数不能为空")
        private Integer createdRetryCount;

        /**
        * 查询报告重试次数
        */
        @NotNull(message = "查询报告重试次数不能为空")
        private Integer queryRetryCount;

        /**
        * 下载报告重试次数
        */
        @NotNull(message = "下载报告重试次数不能为空")
        private Integer downloadRetryCount;

        /**
        * 解析报告重试次数
        */
        @NotNull(message = "解析报告重试次数不能为空")
        private Integer parseRetryCount;


    }


}