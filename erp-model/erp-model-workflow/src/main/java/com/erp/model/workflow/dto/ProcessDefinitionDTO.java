package com.erp.model.workflow.dto;

import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname 流程定义参数
 *
 * @Author Cloud
 * @Date 2023/4/23 10:35
 **/
public class ProcessDefinitionDTO {


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO{

        @NotBlank(message = "流程ID不能为空")
        private String id;

        /**
         * 流程名称
         */
        @NotBlank(message = "流程名称不能为空")
        private String processName;

        /**
         * BPMN流程图
         */
        @NotBlank(message = "BPMN流程图不能为空")
        private String bpmnXml;

        /**
         * 描述信息
         */
        private String remark;

        /**
         * 审核人设置
         */
        @NotBlank(message = "审核人设置不能为空")
        private String reviewSetting;

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessKey;

    }

    @Data
    @NoArgsConstructor
    public static class QueryDTO {
        /**
         * 流程编码
         */
        private String id;
        /**
         * 流程名称
         */
        private String processName;

        /**
         * 流程状态
         */
        private List<String> approveStatus;

        /**
         * 创建人
         */
        private List<String> createUserIds;

        /**
         * 创建时间
         */
        private List<LocalDateTime> createTime;

    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 流程审核状态
         */
        private ApproveStatusEnum approveStatusCode;

        /**
         * 流程审核状态名称
         */
        private String approveStatusName;

        /**
         * BPMN流程图
         */
        private String bpmnXml;

        /**
         * 流程版本
         */
        private Integer processVersion;

        /**
         * 是否已发布
         */
        private Boolean isDeploy;

        /**
         * 描述信息
         */
        private String remark;

        /**
         * 审核人设置
         */
        private String reviewSetting;

        /**
         * 业务类型
         */
        private String businessKey;

        /**
         * 流程单据名称
         */
        private String businessName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTimeList;

    }

    @Data
    @NoArgsConstructor
    public static class DeleteDTO {

        @NotNull(message = "流程定义ID不能为空")
        @NotEmpty(message = "流程定义ID不能为空")
        private List<String> ids;
    }
}
