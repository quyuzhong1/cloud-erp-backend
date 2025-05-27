package com.erp.model.sys.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 三方通知推送记录请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-26
*/
@Data
@NoArgsConstructor
public class ThirdNoticePushRecordDTO implements Serializable {




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
        * cfg_third_notice_id
        */
        private String cfgThirdNoticeId;

        /**
        * 通知类型：messagePush=消息通知,approvalPush=审批推送
        */
        private String noticeType;

        /**
        * 业务主表id
        */
        private String businessId;

        /**
        * 单据名称
        */
        private String businessType;

        /**
        * 单据单号
        */
        private String businessCode;

        /**
        * 通知节点
        */
        private String noticeNode;

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
        * cfg_third_notice_id
        */
        @NotBlank(message = "cfg_third_notice_id不能为空")
        @Size(max = 19,message = "cfg_third_notice_id最大长度不能超过19位")
        private String cfgThirdNoticeId;

        /**
        * 通知类型：messagePush=消息通知,approvalPush=审批推送
        */
        @NotBlank(message = "通知类型：messagePush=消息通知,approvalPush=审批推送不能为空")
        @Size(max = 50,message = "通知类型：messagePush=消息通知,approvalPush=审批推送最大长度不能超过50位")
        private String noticeType;

        /**
        * 业务主表id
        */
        @NotBlank(message = "业务主表id不能为空")
        @Size(max = 50,message = "业务主表id最大长度不能超过50位")
        private String businessId;

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
        * 通知节点
        */
        @NotBlank(message = "通知节点不能为空")
        @Size(max = 50,message = "通知节点最大长度不能超过50位")
        private String noticeNode;

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


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;
        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {

        /**
         * cfg_third_notice_id
         */
        private String cfgThirdNoticeId;

        /**
         * 通知类型：messagePush=消息通知,approvalPush=审批推送
         */
        private String noticeType;
        private String noticeTypeName;

        /**
         * 业务主表id
         */
        private String businessId;

        /**
         * 单据名称
         */
        private String businessType;
        private String businessTypeName;

        /**
         * 单据单号
         */
        private String businessCode;


        /**
         * 提醒方式
         */
        private String noticeMethod;
        private String noticeMethodName;

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
        private String statusName;

        /**
         * 失败原因
         */
        private String errorReason;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

    }

}