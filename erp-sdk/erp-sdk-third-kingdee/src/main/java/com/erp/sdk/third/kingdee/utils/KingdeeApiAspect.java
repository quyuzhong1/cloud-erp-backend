package com.erp.sdk.third.kingdee.utils;

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
        boolean exists = false;
    	try {
    		exists = K3CloudApiThreadLocal.set();
            return pjp.proceed();
        } catch (Exception e) {
            log.error("金蝶API切面错误", e);
            throw e;
        } finally {
        	if(!exists) {
        		K3CloudApiThreadLocal.remove();
        	}
        }
    }

}