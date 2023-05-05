package com.erp.model.workflow.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.model.workflow.enums.DictBasicEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname 流程定义参数
 *
 * @Author Cloud
 * @Date 2023/4/23 10:35
 **/
public class ProcessDefinitionDTO {


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO implements Serializable {

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
        @NotNull(message = "审核人设置不能为空")
        private DictBasicEnum reviewSetting;

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
         * 创建人
         */
        private List<String> createUserIds;

        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;

    }

    @Data
    @NoArgsConstructor
    public static class QueryExportDTO {
        /**
         * 流程编码
         */
        private List<String> ids;
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
        private List<LocalDateTime> createTimeList;

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
        private LocalDateTime createTime;

    }

    @Data
    @NoArgsConstructor
    public static class DeleteDTO {

        @NotNull(message = "流程定义ID不能为空")
        @NotEmpty(message = "流程定义ID不能为空")
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class UnApproveDTO {

        @NotNull(message = "流程定义ID不能为空")
        @NotEmpty(message = "流程定义ID不能为空")
        private String id;
    }


    /**
     * 提交审核入参
     * @Author Cloud
     */
    @Data
    @NoArgsConstructor
    public static class SubmitDTO {
        @NotNull(message = "流程定义ID不能为空")
        @NotEmpty(message = "流程定义ID不能为空")
        private String id;
    }
    @Data
    @NoArgsConstructor
    public class CancelDTO {

        @NotNull(message = "流程定义ID不能为空")
        @NotEmpty(message = "流程定义ID不能为空")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CopyDTO {
        @NotNull(message = "流程定义ID不能为空")
        @NotEmpty(message = "流程定义ID不能为空")
        private String id;
    }


    @Data
    @NoArgsConstructor
    public static class CopyResultDTO {
        /**
         * 流程名称
         */
        private String processName;

        /**
         * BPMN流程图
         */
        private String bpmnXml;

        /**
         * 描述信息
         */
        private String remark;

        /**
         * 审核人设置
         */
        private DictBasicEnum reviewSetting;

        /**
         * 业务类型
         */
        private String businessKey;

        public CopyResultDTO(ProcessDefinitionEntity entity, String businessKey) {
            this.processName = entity.getProcessName();
            this.businessKey = businessKey;
            this.bpmnXml = entity.getBpmnXml();
            this.remark = entity.getRemark();
            this.reviewSetting = entity.getReviewSetting();
        }
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        @ColumnWidth(20)
        @ExcelProperty(value = "流程定义ID", index = 0)
        private String id;
        /**
         * 流程名称
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "流程名称", index = 1)
        private String processName;
        /**
         * 流程审核状态
         */
        @ExcelIgnore
        private ApproveStatusEnum approveStatusCode;
        /**
         * 流程审核状态名称
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "流程审核状态", index = 2)
        private String approveStatusName;
        /**
         * BPMN流程图
         */
        @ExcelIgnore
        private String bpmnXml;

        /**
         * 流程版本
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "流程版本", index = 3)
        private Integer processVersion;

        /**
         * 是否已发布
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "是否已发布", index = 4)
        private String isDeploy;

        /**
         * 描述信息
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "描述信息", index = 5)
        private String remark;

        /**
         * 审核人设置
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "审核人设置", index = 6)
        private String reviewSetting;

        /**
         * 流程单据名称
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "流程单据名称", index = 7)
        private String businessName;

        /**
         * 创建人
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "创建人", index = 8)
        private String createUserName;

        /**
         * 创建时间
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "创建时间", index = 9)
        private LocalDateTime createTime;

    }
}
