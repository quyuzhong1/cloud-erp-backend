package com.common.business.mask.aspect;

import com.common.business.mask.MaskScan;
import com.common.business.mask.core.MaskCore;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏切面
 *
 * <p>切点完全对齐 {@link com.common.business.aspect.DictAspect}：拦截所有
 * {@code com.erp.server.*.controller.api.*.*} 方法的返回值，递归扫描后按
 * {@link com.common.business.mask.Mask} 配置脱敏。</p>
 *
 * <p>{@code @Order(50)} 比 DictAspect（默认 0）低，保证字典翻译先执行后再脱敏。</p>
 *
 * <p>跳过条件（任一命中即跳过）：</p>
 * <ul>
 *   <li>方法 {@code @MaskScan(disabled=true)}</li>
 *   <li>当前用户为超级管理员（{@code LoginUser.isSupper == true}）</li>
 *   <li>方法 {@code @MaskScan(permission)} 不为空且当前用户拥有该权限码</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Slf4j
@Aspect
@Component
@Order(50)
public class MaskAspect {

    @Resource
    private MaskCore maskCore;

    @Pointcut("execution(public * com.erp.server.*.controller.api.*.*(..))")
    public void controllerApi() {
    }

    @Around("controllerApi()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (result == null) {
            return null;
        }
        MaskScan scan = methodAnnotation(pjp);
        if (maskCore.shouldSkip(scan)) {
            return result;
        }
        try {
            maskCore.process(result);
        } catch (Throwable e) {
            log.warn("MaskAspect process failed, return original, msg={}", e.getMessage());
        }
        return result;
    }

    private MaskScan methodAnnotation(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            return method.getAnnotation(MaskScan.class);
        } catch (Throwable e) {
            return null;
        }
    }
}
