package com.erp.sdk.fs.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.constant.ThirdConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.sys.dto.FindThirdUserDTO;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.sdk.fs.config.FsProperties;
import com.erp.sdk.fs.dto.LarkResultDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static com.erp.model.sys.vo.FsBatchSendMessageDTO.getTextMessageMap;

/**
 * @Classname FsService

 * @Date 2022-08-22 9:22
 * @Created by yl
 */
@Slf4j
@Component
public class FsService {

    private final FsProperties fsProperties;

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;
    @Autowired
    public FsService(FsProperties fsProperties) {
        this.fsProperties = fsProperties;
    }
    private static final String FS_AUTHORIZATION = "Bearer ";

    private static final String TAG = "tag";
    private static final String TEXT = "text";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * 根据code 获取飞书用户信息
     *
     * @param dto
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-07-20 18:27
     */
    public Map<String, Object> getFsUser(FindThirdUserDTO dto) {

        String redirectUri = fsProperties.getRedirectLoginUri();
        String thirdType = dto.getThirdType();
        if (StringUtils.isNotBlank(thirdType) && ThirdConstants.THIRD_BINDING_TYPE.equals(thirdType)) {
            redirectUri = fsProperties.getRedirectBindingUri();
        }
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("grant_type", ThirdConstants.FS_GRANT_TYPE);
        paramsMap.put("code", dto.getCode());
        paramsMap.put("client_secret", fsProperties.getClientSecret());
        paramsMap.put("client_id", fsProperties.getClientId());
        paramsMap.put("redirect_uri", redirectUri);
        String bodyStr = OkHttpUtils.doPost(ThirdConstants.FS_TOKEN_URL, paramsMap, null);
        try {
            if (StringUtils.isNotBlank(bodyStr)) {
                Map<String, Object> tokenMap = JSON.parseObject(bodyStr, Map.class);
                if (tokenMap.containsKey("access_token")) {
                    String accessToken = tokenMap.get("access_token").toString();
                    String authorization = FS_AUTHORIZATION + accessToken;
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put(AUTHORIZATION, authorization);
                    headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
                    String userStr = OkHttpUtils.doGet(ThirdConstants.FS_USER_URL, null, headerMap);
                    Map<String, Object> userMap = JSON.parseObject(userStr, Map.class);
                    return userMap;
                }
            }
        }catch (Exception e){
            log.error("扫码获取飞书信息出错>>>>>{}",e);
            log.info("bodyStr >>>>>>{}",bodyStr);
        }

        return Collections.emptyMap();
    }


    /**
     * 根据code 获取飞书用户信息
     *
     * @param code
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-07-20 18:27
     */
    public Map<String, Object> getFsUserByCode(String code) {
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            return getUserAccessInfo(tenantAccessToken, code);
        }
        return Collections.emptyMap();
    }


    /**
     * 获取飞书自建应用的 tenant_access_token
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-11 17:00
     */
    public String getFsTenantAccessToken() {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("app_id", fsProperties.getClientId());
        paramsMap.put("app_secret", fsProperties.getClientSecret());
        String bodyStr = OkHttpUtils.doPost(ThirdConstants.FS_TENANT_ACCESS_TOKEN, paramsMap, null);
        if (StringUtils.isNotBlank(bodyStr)) {
            Map<String, Object> tokenMap = JSON.parseObject(bodyStr, Map.class);
            if (tokenMap.containsKey("code") && Integer.valueOf(tokenMap.get("code").toString()) == 0) {
                return tokenMap.get("tenant_access_token").toString();
            }
        }
        return "";
    }

    /**
     * 获取用户的 飞书accessToken
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-11 17:47
     */
    public Map<String, Object> getUserAccessInfo(String appAccessToken, String code) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("grant_type", ThirdConstants.FS_GRANT_TYPE);
        paramsMap.put("code", code);
        Map<String, String> headerMap = new HashMap<>();
        String authorization = FS_AUTHORIZATION + appAccessToken;
        headerMap.put(AUTHORIZATION, authorization);
        headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
        String userStr = OkHttpUtils.doPostJson(ThirdConstants.FS_USER_ACCESS_TOKEN, paramsMap, headerMap);
        Map<String, Object> userMap = JSON.parseObject(userStr, Map.class);
        return userMap;

    }

    /**
     * 获取飞书的 client id
     *
     * @return
     */
    public String getFsClientId() {
        return fsProperties.getClientId();
    }

    /**
     * 批量发送消息
     *
     * @param
     * @return
     * @author yl
     * @date 2022-11-15 10:27
     */
    public Boolean sendMessage(FsBatchSendMessageDTO dto) {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            Map<String, String> headerMap = new HashMap<>();
            String authorization = FS_AUTHORIZATION + tenantAccessToken;
            headerMap.put(AUTHORIZATION, authorization);
            headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("msg_type", ThirdConstants.FS_MESSAGE_INTERACTIVE);
            //用户的unionIds
            bodyMap.put("union_ids", dto.getUnionIds());
            bodyMap.put("card", dto.getContentMap());
            String resultStr = OkHttpUtils.doPostJson(ThirdConstants.FS_BATCH_SEND_MESSAGE_URL, bodyMap, headerMap);
            Map<String, Object> resultMap = JSON.parseObject(resultStr, Map.class);
            if (resultMap != null && resultMap.containsKey("code")) {
                Integer code = (Integer) resultMap.get("code");
                int succeedCode = 0;
                if (succeedCode == code) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 发送消息
     * @param unionId
     * @param titleContent
     * @param textContent
     * @param msgType
     * @return
     */
    public LarkResultDTO<T> sendMessage(String unionId, String titleContent, String textContent,String msgType) {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("批量发送飞书消息失败 token为空 ={}", JSONUtil.toJsonStr(tenantAccessToken));
            throw new ServiceException(ApiError.ERROR_LARK_TOKEN_IS_NULL);
        }
        Map<String, String> headerMap = new HashMap<>();
        String authorization = FS_AUTHORIZATION + tenantAccessToken;
        headerMap.put(AUTHORIZATION, authorization);
        headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("msg_type", msgType);
        //用户的unionIds
        bodyMap.put("receive_id", unionId);
        String content;
        switch (msgType){
            case ThirdConstants.FS_MESSAGE_INTERACTIVE:
                content = JSONUtil.toJsonStr(FsBatchSendMessageDTO.getCardMessageMap(titleContent, textContent, fsAppUrl));
                break;
            default:
                content = JSONUtil.toJsonStr(getTextMessageMap(textContent));
        }
        bodyMap.put(CONTENT, content);
        String resultStr = OkHttpUtils.doPostJson(ThirdConstants.LARK_SEND_MESSAGE_URL, bodyMap, headerMap);
        LarkResultDTO<T> resultMap = JSONUtil.toBean(resultStr, LarkResultDTO.class);

        if(null == resultMap || 0 != resultMap.getCode()){
            log.error("批量发送飞书消息失败 result ={}", JSONUtil.toJsonStr(resultMap));
            throw new ServiceException(ApiError.ERROR_LARK_SEND_MSG_FAIL);
        }
        return resultMap;
    }

    public void pressMessage(String messageId, List<String> unionIds) {
        if(CharSequenceUtil.isBlank(messageId) || CollUtil.isEmpty(unionIds)){
            throw new ServiceException(ApiError.ERROR_MSG_ID_OR_UNION_ID_IS_NULL);
        }
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("批量加急飞书消息失败 token为空 ={}", JSONUtil.toJsonStr(tenantAccessToken));
            throw new ServiceException(ApiError.ERROR_LARK_TOKEN_IS_NULL);
        }
        String authorization = FS_AUTHORIZATION + tenantAccessToken;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("user_id_list", unionIds);
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(paramMap));
        Request request = new Request.Builder()
                .url(CharSequenceUtil.format(ThirdConstants.LARK_PRESS_URL,messageId))
                .method("PATCH", body)
                .addHeader(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE)
                .addHeader(AUTHORIZATION, authorization)
                .build();
        try {
            Response response = client.newCall(request).execute();
            LarkResultDTO<T> larkResultDTO = JSON.parseObject(response.body().string(), LarkResultDTO.class);
            if(0 != larkResultDTO.getCode()){
                log.error(CharSequenceUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(larkResultDTO)));
                throw new IllegalArgumentException(CharSequenceUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(larkResultDTO)));
            }
        } catch (IOException e) {
            log.error("调用发送应急消息方法失败url={}", CharSequenceUtil.format(ThirdConstants.LARK_PRESS_URL,messageId), e);
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * 获取飞书消息卡片信息
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-18 12:31
     */
    public Map<String, Object> getCardMessageMap(String messageContent, String productContent, String url,boolean isPress) {
        Map<String, Object> cardMap = new LinkedHashMap<>();
        Map<String, Boolean> configMap = new HashMap<>();
        configMap.put("wide_screen_mode", true);
        cardMap.put("config", configMap);
        Map<String, Object> headerMap = new HashMap<>();
        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("tag", "plain_text");
        titleMap.put(CONTENT, messageContent);
        headerMap.put(TITLE, titleMap);
        cardMap.put("header", headerMap);
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> fieldAllMap = new LinkedHashMap<>();
        fieldAllMap.put("tag", "div");
        List<Map<String, Object>> fieldMapList = new ArrayList<>();
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("is_short", true);
        Map<String, Object> textMap = new HashMap<>();
        textMap.put(TAG, "lark_md");
        textMap.put(CONTENT, productContent);
        fieldMap.put(TEXT, textMap);
        fieldMapList.add(fieldMap);
        fieldAllMap.put("fields", fieldMapList);
        elements.add(fieldAllMap);
        if(isPress){
            Map<String, Object> actionAllMap = new LinkedHashMap<>();
            actionAllMap.put( TAG, "action");
            actionAllMap.put("layout", "bisected");
            List<Map<String, Object>> actionList = new ArrayList<>();
            Map<String, Object> actionMap = new LinkedHashMap<>();
            actionMap.put( TAG, "button");
            actionMap.put("url", url);
            actionMap.put("type", "primary");
            Map<String, Object> actionTextMap = new HashMap<>();
            actionTextMap.put( TAG, "plain_text");
            actionTextMap.put(CONTENT, "查看详情");
            actionMap.put("text", actionTextMap);
            Map<String, Object> actionValueMap = new HashMap<>();
            actionValueMap.put("chosen", "approve");
            actionMap.put("value", actionValueMap);
            actionList.add(actionMap);
            actionAllMap.put("actions", actionList);
            elements.add(actionAllMap);
        }
        cardMap.put("elements", elements);
        return cardMap;
    }
}
