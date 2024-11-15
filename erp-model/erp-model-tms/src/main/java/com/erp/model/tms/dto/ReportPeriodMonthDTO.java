package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 核算期间月份表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class ReportPeriodMonthDTO implements Serializable {




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
        * 核算月份
        */
        private LocalDate month;

        /**
        * 分摊组织id
        */
        private String orgId;

        /**
        * 分摊组织名称
        */
        private String orgName;


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
        * 核算月份
        */
        private LocalDate month;

        /**
        * 分摊组织id
        */
        @NotBlank(message = "分摊组织id不能为空")
        @Size(max = 19,message = "分摊组织id最大长度不能超过19位")
        private String orgId;

        /**
        * 分摊组织名称
        */
        @NotBlank(message = "分摊组织名称不能为空")
        @Size(max = 200,message = "分摊组织名称最大长度不能超过200位")
        private String orgName;


    }


    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        private String id;
        /**
         * 核算月份
         */
        private LocalDate reportPeriodMonth;

        /**
         * 分摊组织id
         */
        private String orgId;

        /**
         * 分摊组织名称
         */
        private String orgName;
        /**
         * 核算月份【导出使用】
         */
        private String reportPeriodStr;
    }

    @Data
    @NoArgsConstructor
    public static class QueryDTO {
        /**
         * 重量分摊id
         */
        @NotEmpty(message = "重量分摊ids不能为空")
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        private String id;
        private String reportPeriodStr;
        private boolean disabled;
    }
}