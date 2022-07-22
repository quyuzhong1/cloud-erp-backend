package com.cloud.erp.thirdparty.controller.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.cloud.erp.common.constant.ThirdConstants;
import com.cloud.erp.common.utils.OkHttpUtils;
import com.cloud.erp.thirdparty.config.FsProperties;
import com.cloud.erp.thirdparty.constant.LoginConstant;
import com.cloud.erp.thirdparty.controller.service.FsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname FsServiceImpl
 * @Description TODO
 * @Date 2022-07-20 18:24
 * @Created by yl
 */
@Service
public class FsServiceImpl implements FsService {

    @Autowired
    private FsProperties fsProperties;


    /**
     * 根据code 获取飞书用户信息
     *
     * @param code
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-07-20 18:27
     */
    @Override
    public Map<String, Object> getFsUser(String code) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("grant_type", ThirdConstants.FS_GRANT_TYPE);
        paramsMap.put("code", code);
        paramsMap.put("client_secret", fsProperties.getClientSecret());
        paramsMap.put("client_id", fsProperties.getClientId());
        paramsMap.put("redirect_uri", fsProperties.getRedirectUri());
        String bodyStr = OkHttpUtils.doPost(ThirdConstants.FS_TOKEN_URL, paramsMap, null);
        if (StringUtils.isNotBlank(bodyStr)) {
            Map<String, Object> tokenMap = JSONObject.parseObject(bodyStr, Map.class);
            if (tokenMap.containsKey("access_token")) {
                String accessToken = tokenMap.get("access_token").toString();
                String authorization = LoginConstant.FS_AUTHORIZATION + accessToken;
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("Authorization",authorization);
                headerMap.put("Content-Type",ThirdConstants.CONTENT_TYPE);
                String userStr = OkHttpUtils.doGet(ThirdConstants.FS_USER_URL, null, headerMap);
                System.out.println("userStr======="+userStr);
                Map<String, Object> userMap = JSONObject.parseObject(userStr, Map.class);
                return userMap;
            }

        }
        return null;
    }
}
