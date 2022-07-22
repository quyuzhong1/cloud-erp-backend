package com.cloud.erp.admin.modules.interceptor;


import com.alibaba.fastjson2.JSONObject;
import com.cloud.erp.common.common.token.vo.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Classname 系统管理 拦截器
 * @Description TODO
 * @Date 2022-07-15 17:12
 * @Created by yl
 */
public class SysInterceptor implements HandlerInterceptor {
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String tokenUserStr=request.getHeader("tokenUserInfo");
        System.out.println("================"+tokenUserStr);
        if(StringUtils.isNotBlank(tokenUserStr)){
            LoginUser  user= JSONObject.parseObject(tokenUserStr,LoginUser.class);
            threadLocal.set(user);
        }
        return  true;
    }


}
