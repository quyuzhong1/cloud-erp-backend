package com.erp.common.business.aspect;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.core.utils.ObjectUtils;
import com.common.core.utils.StrUtils;
import com.erp.common.business.annotation.DataPermission;
import com.erp.common.business.annotation.FieldValid;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.UserRequestPermissionsDTO;
import com.erp.common.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 数据过滤处理
 */
@Aspect
public class FieldValidAspect {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ApplicationContext applicationContext;

    // 配置织入点
    @Pointcut("@annotation(com.erp.common.business.annotation.FieldValid)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        // 获得注解
        FieldValid controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null) {
            return;
        }
        String userId = "";
        String userName = "";
        LoginUser userInfo = CommonInterceptor.threadLocal.get();
        if (Objects.isNull(userInfo)) {
            userInfo = new LoginUser();
            userInfo.setUid(userId);
            userInfo.setUserName(userName);
        }
        //当用户id 不为空的时候
        if (StringUtils.isNotBlank(userInfo.getUid())) {
            dataScopeFilter(joinPoint, userInfo, controllerDataScope);
        } else {
            throw new ServiceException(ApiError.ERROR_403);
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private FieldValid getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(FieldValid.class);
        }
        return null;
    }

    /**
     * 数据范围过滤
     *
     * @param joinPoint           切点
     * @param user                用户
     * @param controllerDataScope 自定义注解参数
     */
    public void dataScopeFilter(JoinPoint joinPoint, LoginUser user, FieldValid controllerDataScope) {

    }



}
