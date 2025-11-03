package com.erp.model.dmp.dto;

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
 * ads推送任务请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-10-28
*/
@Data
@NoArgsConstructor
public class AdsPushTaskDTO implements Serializable {




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
        * 任务id
        */
        private String taskId;

        /**
        * 流程 ID
        */
        private String flowId;

        /**
        * 节点 ID
        */
        private String nodeId;

        /**
        * 实例ID/任务ID
        */
        private String instanceId;

        /**
        * 唯一编码
        */
        private String uniqueCode;

        /**
        * 数据MD5
        */
        private String dataEncrypt;

        /**
        * 来源平台
        */
        private String sourcePlatformName;

        /**
        * 目标平台
        */
        private String targetPlatformName;

        /**
        * 业务主题
        */
        private String billTopic;

        /**
        * 单据主键
        */
        private String billKey;

        /**
        * 上游单据业务主题
        */
        private String parentBillTopic;

        /**
        * 上游单据主键(多个英文,拼接)
        */
        private String parentBillKey;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 推送状态：init=待推送,finish=推送成功,error=推送失败
        */
        private String status;

        /**
        * 请求报文
        */
        private String requestData;

        /**
        * 响应报文
        */
        private String responseData;

        /**
        * 错误次数
        */
        private Integer errorCount;

        /**
        * 无需同步状态：push=需要推送，black=黑名单无需推送，self=工人无需推送
        */
        private String pushStatus;

        /**
        * 推送消费流程url
        */
        private String pushFlowUrl;


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
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 64,message = "任务id最大长度不能超过64位")
        private String taskId;

        /**
        * 流程 ID
        */
        @NotBlank(message = "流程 ID不能为空")
        @Size(max = 64,message = "流程 ID最大长度不能超过64位")
        private String flowId;

        /**
        * 节点 ID
        */
        @NotBlank(message = "节点 ID不能为空")
        @Size(max = 64,message = "节点 ID最大长度不能超过64位")
        private String nodeId;

        /**
        * 实例ID/任务ID
        */
        @NotBlank(message = "实例ID/任务ID不能为空")
        @Size(max = 64,message = "实例ID/任务ID最大长度不能超过64位")
        private String instanceId;

        /**
        * 唯一编码
        */
        @NotBlank(message = "唯一编码不能为空")
        @Size(max = 64,message = "唯一编码最大长度不能超过64位")
        private String uniqueCode;

        /**
        * 数据MD5
        */
        @NotBlank(message = "数据MD5不能为空")
        @Size(max = 64,message = "数据MD5最大长度不能超过64位")
        private String dataEncrypt;

        /**
        * 来源平台
        */
        @NotBlank(message = "来源平台不能为空")
        @Size(max = 64,message = "来源平台最大长度不能超过64位")
        private String sourcePlatformName;

        /**
        * 目标平台
        */
        @NotBlank(message = "目标平台不能为空")
        @Size(max = 64,message = "目标平台最大长度不能超过64位")
        private String targetPlatformName;

        /**
        * 业务主题
        */
        @NotBlank(message = "业务主题不能为空")
        @Size(max = 64,message = "业务主题最大长度不能超过64位")
        private String billTopic;

        /**
        * 单据主键
        */
        @NotBlank(message = "单据主键不能为空")
        @Size(max = 255,message = "单据主键最大长度不能超过255位")
        private String billKey;

        /**
        * 上游单据业务主题
        */
        @NotBlank(message = "上游单据业务主题不能为空")
        @Size(max = 64,message = "上游单据业务主题最大长度不能超过64位")
        private String parentBillTopic;

        /**
        * 上游单据主键(多个英文,拼接)
        */
        @NotBlank(message = "上游单据主键(多个英文,拼接)不能为空")
        @Size(max = 255,message = "上游单据主键(多个英文,拼接)最大长度不能超过255位")
        private String parentBillKey;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 64,message = "来源id最大长度不能超过64位")
        private String sourceId;

        /**
        * 推送状态：init=待推送,finish=推送成功,error=推送失败
        */
        @NotBlank(message = "推送状态：init=待推送,finish=推送成功,error=推送失败不能为空")
        @Size(max = 20,message = "推送状态：init=待推送,finish=推送成功,error=推送失败最大长度不能超过20位")
        private String status;

        /**
        * 请求报文
        */
        @NotBlank(message = "请求报文不能为空")
        private String requestData;

        /**
        * 响应报文
        */
        @NotBlank(message = "响应报文不能为空")
        private String responseData;

        /**
        * 错误次数
        */
        @NotNull(message = "错误次数不能为空")
        private Integer errorCount;

        /**
        * 无需同步状态：push=需要推送，black=黑名单无需推送，self=工人无需推送
        */
        @NotBlank(message = "无需同步状态：push=需要推送，black=黑名单无需推送，self=工人无需推送不能为空")
        @Size(max = 20,message = "无需同步状态：push=需要推送，black=黑名单无需推送，self=工人无需推送最大长度不能超过20位")
        private String pushStatus;

        /**
        * 推送消费流程url
        */
        @NotBlank(message = "推送消费流程url不能为空")
        @Size(max = 100,message = "推送消费流程url最大长度不能超过100位")
        private String pushFlowUrl;


    }


}