package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
@Data
@NoArgsConstructor
public class ThirdProcessManagementDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 流程定义ID
         */
        private String processDefinitionId;

        /**
         * 系统用户id
         */
        private String sysUserId;

        /**
         * 三方用户id
         */
        private String thirdUserId;

        /**
         * 业务ID
         */
        private String businessId;

        /**
         * 业务编码
         */
        private String businessCode;

        /**
         * 单据类型
         */
        private String businessKey;

        /**
         * 流程状态
         */
        private String status;

        /**
         * 审批名称
         */
        private String processInstanceName;

        /**
         * 来源平台
         */
        private String sourcePlatform;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 审批任务列表
         */
        List<ThirdProcessTaskManagementDTO.AddDTO> taskList;
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

        /**
         * 审批任务列表
         */
        List<ThirdProcessTaskManagementDTO.UpdateDTO> taskList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 流程实例ID
         */
        @NotBlank(message = "流程实例ID不能为空")
        @Size(max = 64, message = "流程实例ID最大长度不能超过64位")
        private String processInstanceId;

        /**
         * 流程定义ID
         */
        @NotBlank(message = "流程定义ID不能为空")
        @Size(max = 64, message = "流程定义ID最大长度不能超过64位")
        private String processDefinitionId;

        /**
         * 系统用户id
         */
        @NotBlank(message = "系统用户id不能为空")
        @Size(max = 32, message = "系统用户id最大长度不能超过32位")
        private String sysUserId;

        /**
         * 三方用户id
         */
        @NotBlank(message = "三方用户id不能为空")
        @Size(max = 32, message = "三方用户id最大长度不能超过32位")
        private String thirdUserId;

        /**
         * 业务ID
         */
        @NotBlank(message = "业务ID不能为空")
        @Size(max = 32, message = "业务ID最大长度不能超过32位")
        private String businessId;

        /**
         * 业务编码
         */
        @NotBlank(message = "业务编码不能为空")
        @Size(max = 100, message = "业务编码最大长度不能超过100位")
        private String businessCode;

        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 64, message = "单据类型最大长度不能超过64位")
        private String businessKey;

        /**
         * 流程状态
         */
        @NotBlank(message = "流程状态不能为空")
        @Size(max = 20, message = "流程状态最大长度不能超过20位")
        private String status;

        /**
         * 审批名称
         */
        @NotBlank(message = "审批名称不能为空")
        @Size(max = 255, message = "审批名称最大长度不能超过255位")
        private String processInstanceName;

        /**
         * 来源平台
         */
        @NotBlank(message = "来源平台不能为空")
        @Size(max = 32, message = "来源平台最大长度不能超过32位")
        private String sourcePlatform;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;


    }


}