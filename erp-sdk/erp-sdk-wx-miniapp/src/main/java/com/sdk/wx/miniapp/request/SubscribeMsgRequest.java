package com.sdk.wx.miniapp.request;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jack
 * @date 2024-04-11
 */
@Data
public class SubscribeMsgRequest implements Serializable {

    private String touser;

    private String template_id;

    private String page;

    private String miniprogram_state;

    private String lang;

    private Map<String, DataItem> data = new HashMap<>();


    @Data
    public static class DataItem {
        private String value;
    }

}
