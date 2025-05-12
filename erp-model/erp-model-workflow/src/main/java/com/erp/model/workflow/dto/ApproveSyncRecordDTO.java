package com.erp.model.workflow.dto;

import java.time.LocalDateTime;
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
 * ERP审批同步-通知配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ApproveSyncRecordDTO implements Serializable {




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
        private String cfgApproveSyncId;

        /**
        * 通知类型：messagePush=消息通知,approvalPush=审批推送
        */
        private String noticeType;

        /**
        * 单据名称
        */
        private String businessType;

        /**
        * 单据单号
        */
        private String businessCode;

        /**
        * 提醒方式
        */
        private String noticeMethod;

        /**
        * 接收人id
        */
        private String receiverId;

        /**
        * 接收人
        */
        private String receiverName;

        /**
        * 发送时间
        */
        private LocalDateTime sendTime;

        /**
        * 通知标题
        */
        private String title;

        /**
        * 状态：success=推送成功, failed=推送失败
        */
        private String status;

        /**
        * 失败原因
        */
        private String errorReason;


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
        private String cfgApproveSyncId;

        /**
        * 通知类型：messagePush=消息通知,approvalPush=审批推送
        */
        @NotBlank(message = "通知类型：messagePush=消息通知,approvalPush=审批推送不能为空")
        @Size(max = 50,message = "通知类型：messagePush=消息通知,approvalPush=审批推送最大长度不能超过50位")
        private String noticeType;

        /**
        * 单据名称
        */
        @NotBlank(message = "单据名称不能为空")
        @Size(max = 50,message = "单据名称最大长度不能超过50位")
        private String businessType;

        /**
        * 单据单号
        */
        @NotBlank(message = "单据单号不能为空")
        @Size(max = 50,message = "单据单号最大长度不能超过50位")
        private String businessCode;

        /**
        * 提醒方式
        */
        @NotBlank(message = "提醒方式不能为空")
        @Size(max = 50,message = "提醒方式最大长度不能超过50位")
        private String noticeMethod;

        /**
        * 接收人id
        */
        @NotBlank(message = "接收人id不能为空")
        @Size(max = 19,message = "接收人id最大长度不能超过19位")
        private String receiverId;

        /**
        * 接收人
        */
        @NotBlank(message = "接收人不能为空")
        @Size(max = 50,message = "接收人最大长度不能超过50位")
        private String receiverName;

        /**
        * 发送时间
        */
        @NotNull(message = "发送时间不能为空")
        private LocalDateTime sendTime;

        /**
        * 通知标题
        */
        @NotBlank(message = "通知标题不能为空")
        @Size(max = 50,message = "通知标题最大长度不能超过50位")
        private String title;

        /**
        * 状态：success=推送成功, failed=推送失败
        */
        @NotBlank(message = "状态：success=推送成功, failed=推送失败不能为空")
        @Size(max = 50,message = "状态：success=推送成功, failed=推送失败最大长度不能超过50位")
        private String status;

        /**
        * 失败原因
        */
        @NotBlank(message = "失败原因不能为空")
        @Size(max = 200,message = "失败原因最大长度不能超过200位")
        private String errorReason;


    }


}