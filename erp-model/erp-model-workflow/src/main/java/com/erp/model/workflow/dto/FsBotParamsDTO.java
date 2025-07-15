package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Classname FindThirdUserInfo

 * @Date 2022-07-26 10:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FsBotParamsDTO implements Serializable {


    /**
     * 发送审批 Bot 消息的参数
     * 参数字段可参考 https://open.feishu.cn/document/server-docs/approval-v4/message/send-bot-messages#8f6c4704
     */
    @Data
    @NoArgsConstructor
    public static class SendParamsDTO {

        private String templateId;
        private String userId;
        private String thirdUserId;
        private String uuid;
        private String approvalName;
        private String titleUserId;
        private String titleThirdUserId;
        private String titleUserIdType;
        private String comment;
        private String note;
        private String actionDetailUrl;
        private List<String> summaries;
        private String actionCallbackUrl;
        private String actionCallbackToken;
        private String actionCallbackKey;
        private String actionContext;
    }

}
