package com.erp.sdk.fs.service;

import com.alibaba.fastjson2.JSONObject;
import com.common.core.constant.ThirdConstants;
import com.common.core.utils.OkHttpUtils;
import com.erp.common.modules.sys.dto.FindThirdUserDTO;
import com.erp.common.modules.third.dto.FsBatchSendMessageDTO;
import com.erp.sdk.fs.config.FsProperties;
import com.erp.sdk.fs.constant.LoginConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname FsService
 * @Description TODO
 * @Date 2022-08-22 9:22
 * @Created by yl
 */
@Component
public class FsService {

    @Autowired
    private FsProperties fsProperties;


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
        if (StringUtils.isNotBlank(bodyStr)) {
            Map<String, Object> tokenMap = JSONObject.parseObject(bodyStr, Map.class);
            if (tokenMap.containsKey("access_token")) {
                String accessToken = tokenMap.get("access_token").toString();
                String authorization = LoginConstant.FS_AUTHORIZATION + accessToken;
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("Authorization", authorization);
                headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
                String userStr = OkHttpUtils.doGet(ThirdConstants.FS_USER_URL, null, headerMap);
                Map<String, Object> userMap = JSONObject.parseObject(userStr, Map.class);
                return userMap;
            }

        }
        return null;
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
        return null;
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
            Map<String, Object> tokenMap = JSONObject.parseObject(bodyStr, Map.class);
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
        String authorization = LoginConstant.FS_AUTHORIZATION + appAccessToken;
        headerMap.put("Authorization", authorization);
        headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
        String userStr = OkHttpUtils.doPostJson(ThirdConstants.FS_USER_ACCESS_TOKEN, paramsMap, headerMap);
        Map<String, Object> userMap = JSONObject.parseObject(userStr, Map.class);
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
    public Boolean batchSendMessage(FsBatchSendMessageDTO dto) {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            Map<String, String> headerMap = new HashMap<>();
            String authorization = LoginConstant.FS_AUTHORIZATION + tenantAccessToken;
            headerMap.put("Authorization", authorization);
            headerMap.put("Content-Type", ThirdConstants.CONTENT_TYPE);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("msg_type", ThirdConstants.FS_MESSAGE_TEXT);
            //用户的unionIds
            bodyMap.put("union_ids", dto.getUnionIds());
            bodyMap.put("content", dto.getContentMap());
            String resultStr = OkHttpUtils.doPostJson(ThirdConstants.FS_BATCH_SEND_MESSAGE_URL, bodyMap, headerMap);
            Map<String, Object> resultMap = JSONObject.parseObject(resultStr, Map.class);
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

}
