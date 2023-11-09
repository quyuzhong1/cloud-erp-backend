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
 * @since 2023-11-08
*/
@Data
@NoArgsConstructor
public class ReportScheduleDTO implements Serializable {




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
        private String reportScheduleId;

        /**
        * 市场IDS
        */
        private String marketplaceIds;

        /**
        * OMS店铺ID
        */
        private String shopId;

        /**
        * 报告生成间隔时间
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
        * 是否禁用 false 未禁用
        */
        private Boolean disabled;


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
        private String reportScheduleId;

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
        * 报告生成间隔时间
        */
        @NotBlank(message = "报告生成间隔时间不能为空")
        @Size(max = 10,message = "报告生成间隔时间最大长度不能超过10位")
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
        * 是否禁用 false 未禁用
        */
        @NotNull(message = "是否禁用 false 未禁用不能为空")
        private Boolean disabled;


    }


}