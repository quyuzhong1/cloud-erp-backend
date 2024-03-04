package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 亚马逊报告计划表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@NoArgsConstructor
public class AmzReportScheduleDTO implements Serializable {




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
        * 报告计划ID
        */
        private String amzReportScheduleId;

        /**
        * 市场IDS
        */
        private String marketplaceIds;

        /**
        * OMS店铺ID
        */
        private String shopId;

        /**
        * 报告生成间隔时间:PT5M，PT15M，PT30M，PT1H，PT2H，PT4H，PT8H，PT12H，P1D，P2D，P3D，PT84H，P7D，P14D，P15D，P18D，P30D，P1M
        */
        private String period;

        /**
        * 首次创建下次创建报告的时间
        */
        private LocalDateTime firstNextReportCreationTime;

        /**
        * 报告类型
        */
        private String reportType;

        /**
        * 报告类型名称
        */
        private String reportTypeName;

        /**
        * 订阅状态:not=未订阅,wait=待订阅,already=已订阅
        */
        private String subscribedStatus;

        /**
        * 取消状态:none=无(无需取消), wait=待取消, cancel=已取消
        */
        private String cancelStatus;

        /**
        * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)
        */
        private String subscribedType;


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
        * 报告计划ID
        */
        @NotBlank(message = "报告计划ID不能为空")
        @Size(max = 19,message = "报告计划ID最大长度不能超过19位")
        private String amzReportScheduleId;

        /**
        * 市场IDS
        */
        @NotBlank(message = "市场IDS不能为空")
        @Size(max = 100,message = "市场IDS最大长度不能超过100位")
        private String marketplaceIds;

        /**
        * OMS店铺ID
        */
        @NotBlank(message = "OMS店铺ID不能为空")
        @Size(max = 19,message = "OMS店铺ID最大长度不能超过19位")
        private String shopId;

        /**
        * 报告生成间隔时间:PT5M，PT15M，PT30M，PT1H，PT2H，PT4H，PT8H，PT12H，P1D，P2D，P3D，PT84H，P7D，P14D，P15D，P18D，P30D，P1M
        */
        @NotBlank(message = "报告生成间隔时间:PT5M，PT15M，PT30M，PT1H，PT2H，PT4H，PT8H，PT12H，P1D，P2D，P3D，PT84H，P7D，P14D，P15D，P18D，P30D，P1M不能为空")
        @Size(max = 10,message = "报告生成间隔时间:PT5M，PT15M，PT30M，PT1H，PT2H，PT4H，PT8H，PT12H，P1D，P2D，P3D，PT84H，P7D，P14D，P15D，P18D，P30D，P1M最大长度不能超过10位")
        private String period;

        /**
        * 首次创建下次创建报告的时间
        */
        @NotNull(message = "首次创建下次创建报告的时间不能为空")
        private LocalDateTime firstNextReportCreationTime;

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
        * 订阅状态:not=未订阅,wait=待订阅,already=已订阅
        */
        @NotBlank(message = "订阅状态:not=未订阅,wait=待订阅,already=已订阅不能为空")
        @Size(max = 10,message = "订阅状态:not=未订阅,wait=待订阅,already=已订阅最大长度不能超过10位")
        private String subscribedStatus;

        /**
        * 取消状态:none=无(无需取消), wait=待取消, cancel=已取消
        */
        @NotBlank(message = "取消状态:none=无(无需取消), wait=待取消, cancel=已取消不能为空")
        @Size(max = 10,message = "取消状态:none=无(无需取消), wait=待取消, cancel=已取消最大长度不能超过10位")
        private String cancelStatus;

        /**
        * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)
        */
        @NotBlank(message = "订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)不能为空")
        @Size(max = 10,message = "订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)最大长度不能超过10位")
        private String subscribedType;


    }


}