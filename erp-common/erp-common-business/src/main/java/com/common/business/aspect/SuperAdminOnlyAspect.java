package com.common.business.aspect;

import com.common.business.mask.MaskPermissionResolver;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 超级管理员接口鉴权切面。
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SuperAdminOnlyAspect {

    @Autowired(required = false)
    private MaskPermissionResolver permissionResolver;

    @Around("@within(com.common.business.annotation.SuperAdminOnly)"
            + " || @annotation(com.common.business.annotation.SuperAdminOnly)")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        LoginUser user = UserContext.getLoginUser();
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            throw new ServiceException(ApiError.HTTP_UNAUTHORIZED);
        }
        if (Boolean.TRUE.equals(user.getIsSupper())) {
            return joinPoint.proceed();
        }
        if (permissionResolver != null) {
            try {
                if (permissionResolver.isSuperAdmin(user)) {
                    return joinPoint.proceed();
                }
            } catch (Exception e) {
                // 鉴权依赖异常时必须拒绝访问，不能降级放行敏感运维接口。
                log.warn("Super admin permission resolve failed, uid={}, resolver={}, msg={}",
                        user.getUid(), permissionResolver.getClass().getName(), e.getMessage());
            }
        }
        throw new ServiceException(ApiError.HTTP_FORBIDDEN);
    }
}
