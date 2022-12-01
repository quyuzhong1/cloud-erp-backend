package com.cloud.erp.gateway.interceptor;

import com.alibaba.fastjson2.JSONObject;
import com.erp.common.vo.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @Classname plm 拦截器
 * @Description TODO
 * @Date 2022-09-16 11:23
 * @Created by yl
 */
public class PlmInterceptor implements HandlerInterceptor {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tokenUserStr = request.getHeader("tokenUserInfo");
        try {
            if (StringUtils.isNotBlank(tokenUserStr)) {
                tokenUserStr = URLDecoder.decode(tokenUserStr, "UTF-8");
                LoginUser user = JSONObject.parseObject(tokenUserStr, LoginUser.class);
                threadLocal.set(user);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }
}
