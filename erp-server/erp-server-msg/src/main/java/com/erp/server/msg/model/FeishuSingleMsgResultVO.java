package com.erp.server.msg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname: FeishuSingleMsgResultVO

 * @CreateTime: 2023-04-20  17:42
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class FeishuSingleMsgResultVO implements Serializable {

    private String messageId;

    private String rootId;

    private String parentId;

    private String msgType;

    private String createTime;

    private String updateTime;


    private Boolean deleted;

    private Boolean updated;

    private String chatId;


    private String upperMessageId;

    private LarkSender sender;

    private LarkBody body;

    private List<LarkMentions> mentions;

    @Data
    @NoArgsConstructor
    public static class LarkSender implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
        private String id;

        private String idType;

        private String senderType;

        private String tenantKey;
    }
    @Data
    @NoArgsConstructor
    public static class LarkBody implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class LarkMentions implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
        private String key;
        private String id;
        private String name;

        private String tenantKey;
    }

}