package com.erp.model.workflow.dto;

import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 流程相关参数
 *
 * @Author Cloud
 * @Date 2023/4/24 18:11
 **/
public class ProcessDTO {

    private ProcessDTO() {
    }

    /**
     * 部署流程入参
     */
    @Data
    @NoArgsConstructor
    public static class DeployDTO {
        /**
         * 部署流程ID
         */
        @NotBlank(message = "流程定义ID不能为空")
        private String processDefinitionId;

        /**
         * 流程版本
         */
        @NotNull(message = "流程版本不能为空")
        private Integer processVersion;
    }

    /**
     * 流程发布入参
     */


    /**
     * 流程部署出参
     */
    @Data
    @NoArgsConstructor
    public static class DeployResultDTO {
        /**
         * 流程定义ID
         */
        private String processDefinitionId;
        /**
         * 流程定义名称
         */
        private String processName;

        /**
         * 流程版本
         */
        private Integer processVersion;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 备注
         */
        private String remark;

        public DeployResultDTO(ProcessDefinitionEntity definitionEntity, int version) {
            this.processDefinitionId = definitionEntity.getId();
            this.processName = definitionEntity.getProcessName();
            this.processVersion = version;
            this.createTime = definitionEntity.getCreateTime();
            this.updateTime = definitionEntity.getUpdateTime();
            this.remark = definitionEntity.getRemark();
        }
    }


}
