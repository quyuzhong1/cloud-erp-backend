package com.erp.model.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FsCallbackEventDTO implements Serializable {

    /**
     * 审批实例变更事件
     */
    @Data
    @NoArgsConstructor
    public static class ApprovalInstanceEventDTO {
        /**
         * appId
         */
        @JsonProperty("app_id")
        private String appId;
        /**
         * 审批定义编码
         */
        @JsonProperty("approval_code")
        private String approvalCode;
        /**
         * 审批实例id
         */
        @JsonProperty("instance_code")
        private String instanceCode;
        /**
         * 实例操作时间
         */
        @JsonProperty("instance_operate_time")
        private String instanceOperateTime;
        /**
         * 操作时间
         */
        @JsonProperty("operate_time")
        private String operateTime;
        /**
         * 状态
         */
        @JsonProperty("status")
        private String status;
        /**
         * tenantKey
         */
        @JsonProperty("tenant_key")
        private String tenantKey;
        /**
         * 事件类型
         */
        @JsonProperty("approval_instance")
        private String type;
    }
}
