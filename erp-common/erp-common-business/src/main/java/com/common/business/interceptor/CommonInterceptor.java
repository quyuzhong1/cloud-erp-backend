package com.common.business.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.AuthPassPath;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

public class CommonInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tokenUserStr = request.getHeader("tokenUserInfo");
        try {
            if (StringUtils.isNotBlank(tokenUserStr)) {
                tokenUserStr = URLDecoder.decode(tokenUserStr, "UTF-8");
                LoginUser user = JSON.parseObject(tokenUserStr, LoginUser.class);
                UserContext.setLoginUser(user);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        UserContext.setIsUserSystem(false);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //判断请求路径在不在拦截名单中，在直接放行
        // 获取请求URL
        String uri = request.getServletPath();
        List<String> pathList = Arrays.asList(AuthPassPath.PASS_PATH_LIST.split(";"));
        if (!pathList.contains(uri)) {
            UserContext.clear();
            UserContext.clearIsUserSystem();
        }
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
