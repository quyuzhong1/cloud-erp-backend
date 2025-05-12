package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 三方通知配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgThirdNoticeDTO implements Serializable {




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
        * 单据类型
        */
        private String businessType;

        /**
        * 通知节点
        */
        private String noticeNode;

        /**
        * 通知方式：single=单条,summary=汇总
        */
        private String method;

        /**
        * 通知状态
        */
        private Boolean noticeStatus;

        /**
        * 通知标题
        */
        private String title;

        /**
        * 通知类型
        */
        private String noticeType;

        /**
        * 跳转链接
        */
        private String url;

        /**
        * cron
        */
        private String cron;

        /**
        * 通知人员
        */
        private String roleType;

        /**
        * 具体人员
        */
        private String specificPerson;

        /**
        * 推送方式
        */
        private String noticeMethod;


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
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 50,message = "单据类型最大长度不能超过50位")
        private String businessType;

        /**
        * 通知节点
        */
        @NotBlank(message = "通知节点不能为空")
        @Size(max = 50,message = "通知节点最大长度不能超过50位")
        private String noticeNode;

        /**
        * 通知方式：single=单条,summary=汇总
        */
        @NotBlank(message = "通知方式：single=单条,summary=汇总不能为空")
        @Size(max = 50,message = "通知方式：single=单条,summary=汇总最大长度不能超过50位")
        private String method;

        /**
        * 通知状态
        */
        @NotNull(message = "通知状态不能为空")
        private Boolean noticeStatus;

        /**
        * 通知标题
        */
        @NotBlank(message = "通知标题不能为空")
        @Size(max = 200,message = "通知标题最大长度不能超过200位")
        private String title;

        /**
        * 通知类型
        */
        @NotBlank(message = "通知类型不能为空")
        @Size(max = 50,message = "通知类型最大长度不能超过50位")
        private String noticeType;

        /**
        * 跳转链接
        */
        @NotBlank(message = "跳转链接不能为空")
        @Size(max = 500,message = "跳转链接最大长度不能超过500位")
        private String url;

        /**
        * cron
        */
        @NotBlank(message = "cron不能为空")
        @Size(max = 50,message = "cron最大长度不能超过50位")
        private String cron;

        /**
        * 通知人员
        */
        @NotBlank(message = "通知人员不能为空")
        @Size(max = 50,message = "通知人员最大长度不能超过50位")
        private String roleType;

        /**
        * 具体人员
        */
        @NotBlank(message = "具体人员不能为空")
        @Size(max = 1,024,message = "具体人员最大长度不能超过1,024位")
        private String specificPerson;

        /**
        * 推送方式
        */
        @NotBlank(message = "推送方式不能为空")
        @Size(max = 32,message = "推送方式最大长度不能超过32位")
        private String noticeMethod;


    }


}