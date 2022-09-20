package com.erp.server.plm.interceptor;

import com.alibaba.fastjson2.JSONObject;
import com.erp.common.vo.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Classname plm 拦截器
 * @Description TODO
 * @Date 2022-09-16 11:23
 * @Created by yl
 */
public class PlmInterceptor implements HandlerInterceptor {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String tokenUserStr=request.getHeader("tokenUserInfo");
        if(StringUtils.isNotBlank(tokenUserStr)){
            LoginUser user= JSONObject.parseObject(tokenUserStr, LoginUser.class);
            threadLocal.set(user);
        }
        return  true;
    }
}
