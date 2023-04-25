package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程相关参数
 *
 * @Author Cloud
 * @Date 2023/4/24 18:11
 **/
public class ProcessDTO {


    /**
     * 部署流程入参
     */
    @Data
    @NoArgsConstructor
    public static class DeployDTO {
        /**
         * 部署流程ID
         */
        private String processDefinitionId;
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
         * 流程图XMl
         */
        private String bpmnXml;

        /**
         * 创建时间
         */
        private String createTime;
        /**
         * 更新时间
         */
        private String updateTime;
        /**
         * 备注
         */
        private String remark;

        private Boolean result;
    }


}
