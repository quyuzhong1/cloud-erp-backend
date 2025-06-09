package com.common.business.threadlocal;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuruipeng
 * @date 2023年11月16日 17:38
 * 第三方仓上下文
 */
@Slf4j
public class ThirdWarehouseContext {

    private ThirdWarehouseContext() {
    }

    /**
     * 存放调用第三方接口返回JSON
     */
    private static final ThreadLocal<String> responseJson = new ThreadLocal<>();

    /**
     * 存放调用第三方接口请求json
     */
    private static final ThreadLocal<String> requestJson = new ThreadLocal<>();

    /**
     * 存放调用第三方授权信息
     */
    private static final ThreadLocal<Map<String,Object>> authMap = ThreadLocal.withInitial(HashMap::new);

    /**
     * 授权id
     */
    private static final ThreadLocal<String> authId = new ThreadLocal<>();

    /**
     * 接口返回信息
     */
    private static final ThreadLocal<String> msg = new ThreadLocal<>();

    public static void setAuthId(String id) {authId.set(id);}

    public static String getAuthId() {return authId.get();}

    public static void setRequestJson(String json) {requestJson.set(json);}

    public static String getRequestJson() {return requestJson.get();}

    public static void setResponseJson(String json) {responseJson.set(json);}

    public static String getResponseJson() { return responseJson.get();}

    public static void setAuthMap(Map<String,Object> map) {authMap.set(map);}

    public static Map<String,Object> getAuthMap() { return authMap.get();}

    public static void setMsg(String message) {msg.set(message);}

    public static String getMsg() { return msg.get();}

    public static void remove() {
        responseJson.remove();
        requestJson.remove();
        authMap.remove();
        authId.remove();
        msg.remove();
    }
}
