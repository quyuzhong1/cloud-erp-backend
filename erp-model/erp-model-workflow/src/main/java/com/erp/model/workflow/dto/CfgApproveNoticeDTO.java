package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * ERP审批同步-通知配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgApproveNoticeDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 通知类型 /workflow/common/enumDropDown?type=CfgApproveNoticeRoleType
        */
        private String noticeType;

        /**
        * 角色 /workflow/common/enumDropDown?type=CfgApproveNoticeRoleType
        */
        private String roleType;

        /**
        * 具体人员
        */
        private String specificPerson;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 通知类型 /workflow/common/enumDropDown?type=CfgApproveNoticeRoleType
        */
        @NotBlank(message = "通知类型：approve=审批通知,approveResult=审核结果通知,cc=抄送通知,timeoutWarning=超时预警通知,recall=撤回通知不能为空")
        @Size(max = 50,message = "通知类型：approve=审批通知,approveResult=审核结果通知,cc=抄送通知,timeoutWarning=超时预警通知,recall=撤回通知最大长度不能超过50位")
        private String noticeType;

        /**
        * 角色 /workflow/common/enumDropDown?type=CfgApproveNoticeRoleType
        */
        @NotBlank(message = "角色：applicant=申请人,cc=抄送人,approver=审核人不能为空")
        @Size(max = 50,message = "角色：applicant=申请人,cc=抄送人,approver=审核人最大长度不能超过50位")
        private String roleType;

        /**
        * 具体人员
        */
        @NotBlank(message = "具体人员不能为空")
        @Size(max = 1024,message = "具体人员最大长度不能超过1,024位")
        private String specificPerson;


    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class NoticeSettingDTO{
        private String id;
        /**
         * 主表id
         */
        private String mainId;

        /**
         * 通知类型 /workflow/common/enumDropDown?type=CfgApproveNoticeRoleType
         */
        @NotBlank(message = "通知类型不能为空")
        private String noticeType;
        /**
         * 角色 /workflow/common/enumDropDown?type=CfgApproveNoticeRoleType
         */
        private String roleType;
        @NotEmpty
        private List<String> roleTypeList;

        /**
         * 具体人员
         */
        private String specificPerson;
        private List<String> specificPersonList;

        private Boolean enableStatus;
    }

}