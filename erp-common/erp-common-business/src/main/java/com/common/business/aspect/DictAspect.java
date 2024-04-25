package com.common.business.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.common.business.threadlocal.DictThreadLocal;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class DictAspect {

    @Resource
    private DictCore dictCore;

    // 定义切点Pointcut
    @Pointcut("execution(public * com.erp.server.*.controller.api.*.*(..))")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {

        //Dto时区转换
        Object[] args = pjp.getArgs();
        Object result = pjp.proceed(args);
        dictCore.parseDictText(result);
        DictThreadLocal.remove();
        return result;
    }


}
