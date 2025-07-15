package com.erp.model.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FsCallbackApiReqDTO {

    //如果三方审批定义配置了加密密钥（action_callback_key），则以上参数会进行加密后放在该参数内，接收回调后需要使用 key 进行解密。
    private String encrypt;

    @JsonProperty("action_type")
    private String actionType;

    @JsonProperty("action_context")
    private String actionContext;

    @NotBlank(message = "")
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("approval_code")
    private String approvalCode;

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("token")
    private String token;

    @JsonProperty("message_id")
    private String messageId;


}
