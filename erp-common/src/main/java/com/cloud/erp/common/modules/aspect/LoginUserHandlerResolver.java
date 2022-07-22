package com.cloud.erp.common.modules.aspect;

import com.cloud.erp.common.modules.annotation.LoginUser;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * @Classname LoginUserHandlerResolver
 * @Description TODO
 * @Date 2022-07-15 16:12
 * @Created by yl
 */
@Configuration
public class LoginUserHandlerResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String ss = request.getHeader("tokenUserInfo");
        System.out.println("=============" + ss);
        return null;
    }
}
