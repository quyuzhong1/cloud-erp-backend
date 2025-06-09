package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * @Classname FsBatchSendMessageDTO

 * @Date 2022-11-15 11:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FsBatchSendMessageDTO implements Serializable {


    public static final String CONTENT = "content";
    /**
     * 飞书的union_id
     */
    private List<String> unionIds;


    private Map<String, Object> contentMap;


    public static Map<String, Object> getCardMessageMap(String messageContent, String productContent, String url) {

        Map<String, Object> textMap = new HashMap<>();
        textMap.put("tag", "lark_md");
        textMap.put(CONTENT, productContent);

        Map<String, Object> actionTextMap = new HashMap<>();
        actionTextMap.put("tag", "plain_text");
        actionTextMap.put(CONTENT, "查看详情");

        Map<String, Object> actionValueMap = new HashMap<>();
        actionValueMap.put("chosen", "approve");

        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("is_short", true);
        fieldMap.put("text", textMap);

        Map<String, Object> actionMap = new LinkedHashMap<>();
        actionMap.put("tag", "button");
        actionMap.put("url", url);
        actionMap.put("type", "primary");
        actionMap.put("text", actionTextMap);
        actionMap.put("value", actionValueMap);

        List<Map<String, Object>> actionList = new ArrayList<>();

        List<Map<String, Object>> fieldMapList = new ArrayList<>();
        fieldMapList.add(fieldMap);

        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("tag", "plain_text");
        titleMap.put(CONTENT, messageContent);

        Map<String, Object> fieldAllMap = new LinkedHashMap<>();
        fieldAllMap.put("tag", "div");
        fieldAllMap.put("fields", fieldMapList);

        Map<String, Object> actionAllMap = new LinkedHashMap<>();
        actionAllMap.put("tag", "action");
        actionAllMap.put("layout", "bisected");
        actionAllMap.put("actions", actionList);

        Map<String, Boolean> configMap = new HashMap<>();
        configMap.put("wide_screen_mode", true);

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("title", titleMap);

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(fieldAllMap);
        elements.add(actionAllMap);


        Map<String, Object> cardMap = new LinkedHashMap<>();
        cardMap.put("config", configMap);
        cardMap.put("header", headerMap);
        cardMap.put("elements", elements);
        return cardMap;
    }

    public static Map<String, Object> getTextMessageMap(String textContext) {
        Map<String, Object> textMap = new LinkedHashMap<>();
        textMap.put("text", textContext);
        return textMap;
    }
}
