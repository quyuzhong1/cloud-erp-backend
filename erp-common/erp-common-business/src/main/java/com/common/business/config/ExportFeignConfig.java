package com.common.business.config;

import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Order(1)
public class ExportFeignConfig {
    @Bean
    public RequestInterceptor exportWmsRequestInterceptor() {
        return requestTemplate -> {
            try {
                requestTemplate.header("tokenUserInfo", URLEncoder.encode(JSONObject.toJSONString(UserContext.getDefaultLoginUser()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new ServiceException(e.getMessage());
            }
        };
    }
}
