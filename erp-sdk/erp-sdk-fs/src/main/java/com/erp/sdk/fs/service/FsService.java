package com.erp.sdk.fs.service;

import com.alibaba.fastjson2.JSONObject;
import com.common.core.constant.ThirdConstants;
import com.common.core.utils.OkHttpUtils;
import com.erp.common.modules.sys.dto.FindThirdUserDTO;
import com.erp.sdk.fs.config.FsProperties;
import com.erp.sdk.fs.constant.LoginConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
        String thirdType=dto.getThirdType();
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
}
