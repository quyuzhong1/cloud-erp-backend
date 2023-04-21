package com.erp.server.msg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname: FeishuSingleMsgResultVO
 * @Description: TODO
 * @CreateTime: 2023-04-20  17:42
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class FeishuSingleMsgResultVO implements Serializable {

    private String message_id;

    private String root_id;

    private String parent_id;

    private String msg_type;

    private String create_time;

    private String update_time;


    private Boolean deleted;

    private Boolean updated;

    private String chat_id;


    private String upper_message_id;

    private LarkSender sender;

    private LarkBody body;

    private List<LarkMentions> mentions;

    @Data
    @NoArgsConstructor
    public static class LarkSender{
        private String id;

        private String id_type;

        private String sender_type;

        private String tenant_key;
    }
    @Data
    @NoArgsConstructor
    public static class LarkBody{
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class LarkMentions{
        private String key;
        private String id;
        private String name;

        private String tenant_key;
    }

}