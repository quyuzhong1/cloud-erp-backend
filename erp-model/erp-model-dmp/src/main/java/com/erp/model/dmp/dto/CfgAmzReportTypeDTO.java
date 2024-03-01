package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 亚马逊报告类型配置请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@NoArgsConstructor
public class CfgAmzReportTypeDTO implements Serializable {




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
        * 报告类型
        */
        private String reportType;

        /**
        * 备注
        */
        private String remark;

        /**
        * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)
        */
        private String subscribedType;

        /**
        * 报告分组(会互斥的报告类型视为同组)
        */
        private String reportGroup;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 是否全量更新: t=全量更新, f=增量更新
        */
        private Boolean isFullUpdate;

        /**
        * 是否检查先前任务: t=是，f=否
        */
        private Boolean hasPreTask;


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
        * 报告类型
        */
        @NotBlank(message = "报告类型不能为空")
        @Size(max = 100,message = "报告类型最大长度不能超过100位")
        private String reportType;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 100,message = "备注最大长度不能超过100位")
        private String remark;

        /**
        * 订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)
        */
        @NotBlank(message = "订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)不能为空")
        @Size(max = 10,message = "订阅类型:amazon=亚马逊报告计划,manual=手动(定时任务amazonReportJob)最大长度不能超过10位")
        private String subscribedType;

        /**
        * 报告分组(会互斥的报告类型视为同组)
        */
        @NotBlank(message = "报告分组(会互斥的报告类型视为同组)不能为空")
        @Size(max = 32,message = "报告分组(会互斥的报告类型视为同组)最大长度不能超过32位")
        private String reportGroup;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 是否全量更新: t=全量更新, f=增量更新
        */
        @NotNull(message = "是否全量更新: t=全量更新, f=增量更新不能为空")
        private Boolean isFullUpdate;

        /**
        * 是否检查先前任务: t=是，f=否
        */
        @NotNull(message = "是否检查先前任务: t=是，f=否不能为空")
        private Boolean hasPreTask;


    }


}