package com.erp.sdk.third.kingdee.utils;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class KingdeeApiAspect {
	
    @Pointcut("@annotation(com.erp.sdk.third.kingdee.utils.KingdeeApi)")
    public void kingdeeApiPointCut() {
    }

    @Around("kingdeeApiPointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Method method = currentMethod(pjp);
        // 校验 method 是否为 null
        if (method == null) {
            log.error("无法获取当前方法信息，请检查切入点配置");
            throw new IllegalArgumentException("当前方法不存在");
        }
        //获取到方法的注解对象
        KingdeeApi annotation = method.getAnnotation(KingdeeApi.class);
        KingdeePushModuleEnum kingdeePushModuleEnum = annotation.value();
        KingdeeApiUtils kingdeeApiUtils = null;
        try {
			kingdeeApiUtils = KingdeeApiUtilsPool.getKingdeeApiUtils(kingdeePushModuleEnum.getCode());
			KingdeeApiThreadLocal.set(kingdeeApiUtils);
			return pjp.proceed();
		} catch (Exception e) {
			log.info("金蝶API切面错误" , e);
			throw e;
		}finally {
			KingdeeApiUtilsPool.returnKingdeeApiUtils(kingdeeApiUtils);
			KingdeeApiThreadLocal.remove();
		}
    }

    /**
     * 根据切入点获取执行的方法
     * @param joinPoint 切入点
     * @return  获取当前方法
     */
    private Method currentMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        //获取目标类的所有方法，找到当前要执行的方法
        Method[] methods = joinPoint.getTarget().getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        log.warn("未找到方法：{}", methodName);
        // 返回 null 并记录警告日志
        return null;
    }
}