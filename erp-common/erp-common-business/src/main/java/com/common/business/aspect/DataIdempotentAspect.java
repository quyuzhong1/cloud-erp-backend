package com.common.business.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zdy
 * @ClassName IdempotentAspect
 * @description: 数据幂等实现
 * @date 2023年10月25日
 * @version: 1.0
 *
 * 使用示例：
 *     @DataIdempotent(keyIdName = "userDTO")
 *     @DataIdempotent(keyIdName = "jsonObject")
 *     @DataIdempotent(keyIdName = "userDTO.mobile")
 *     @DataIdempotent(keyIdName = "userDTO.mobile,userDTO.realName")
 *     @DataIdempotent(keyIdName = "dto.userDTO.mobile,dto.userDTO.realName")
 *
 */
@Slf4j
@Aspect
@Component
public class DataIdempotentAspect {
    @Resource
    private RedissonClient redissonClient;

    private static final ThreadLocal<List<RLock>> LOCK_THREAD = new ThreadLocal<>();

    @Pointcut("@annotation(com.common.business.annotation.DataIdempotent)")
    public void dataPointCut() {
    }

    @Around("dataPointCut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = currentMethod(proceedingJoinPoint);
        //获取到方法的注解对象
        DataIdempotent idempotent = method.getAnnotation(DataIdempotent.class);
        Parameter[] parameters = method.getParameters();
        //单位 秒
        long leaseTime = idempotent.leaseTime();
        long waitTime = idempotent.waitTime();
        String businessType = idempotent.businessType();
        String keyIdName = idempotent.keyIdName();
        //获取传参
        Object[] obj1 = proceedingJoinPoint.getArgs();

        StringBuilder sb = new StringBuilder();
        String[] split = keyIdName.split(",");

        for (String s : split) {
            String[] fieldNames = s.split("\\.");
            Integer index = getIndex(parameters, fieldNames[0]);
            if (Objects.nonNull(index) && fieldNames.length > 1) {
                Object value = getNestedField(obj1[index], s);
                sb.append(value);
            } else if (Objects.nonNull(index)) {
                //单参时
                Object value = obj1[index];
                sb.append(value);
            }
        }
        log.debug("幂等切面获取参数：" + sb.toString());
        if (StringUtils.isNotEmpty(sb)) {
            List<RLock> rLocks = new ArrayList<>();
            String submitKey = "DataIdempotent:" + sb + "_" + businessType;
            try {
                log.info("分布式锁上锁，key：{}，lockTime：{}", submitKey, leaseTime);
                RLock clientLock = redissonClient.getLock(submitKey);

                //不设置 lockTime watch dog会 默认 锁定30s 10s重试
                boolean locked = clientLock.tryLock(waitTime, TimeUnit.SECONDS);
                if (!locked) {
                    log.error("{}上锁失败", submitKey);
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                rLocks.add(clientLock);
                log.info("分布式锁上锁成功，key：{}，lockTime：{}", submitKey, leaseTime);
            } catch (Exception e) {
                //存在不能上锁情况时 释放已上锁对象
                if (CollectionUtil.isNotEmpty(rLocks)) {
                    // 无需判断锁是否存在，直接调用 unlock
                    rLocks.forEach(rLock -> {
                        if (rLock.isLocked()) {
                            rLock.unlock();
                        }
                    });
                }
                throw new ServiceException(ApiError.ERROR_1026);
            }
            if (CollectionUtil.isNotEmpty(rLocks)) {
                LOCK_THREAD.set(rLocks);
            }
        }
        // 调用目标方法
        return proceedingJoinPoint.proceed();
    }

    /*** 处理完请求后执行
     *  @param joinPoint 切点
     */
    @AfterReturning(value = "dataPointCut()", returning = "apiResult")
    public void doAfterReturning(JoinPoint joinPoint, Object apiResult) {
        handleData();
    }

    /*** 拦截异常操作
     * ** @param joinPoint 切点
     * * @param e         异常
     * */
    @AfterThrowing(value = "dataPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleData();
    }

    /**
     * 根据切入点获取执行的方法
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
        return null;
    }

    private void handleData() {
        List<RLock> rLocks = LOCK_THREAD.get();
        if (CollectionUtil.isNotEmpty(rLocks)) {
            try {
                rLocks.forEach(rLock -> {
                    log.info("任务执行完成，当前锁状态：{}", rLock.isLocked());
                    // 无需判断锁是否存在，直接调用 unlock
                    if (rLock.isLocked()) {
                        rLock.unlock();
                        log.info("释放锁");
                    }
                });
            } catch (Exception exception) {
                throw new ServiceException(ApiError.ERROR_1026);
            } finally {
                LOCK_THREAD.remove();
            }
        }
    }

    private Object getNestedField(Object obj, String fieldName) {
        String[] fieldNames = fieldName.split("\\.");
        try {
            Object value = "";
            value = ReflectUtil.getFieldValue(obj,fieldNames[1]);
            return value;
        } catch (Exception e) {
            return "";
        }

    }
    private Integer getIndex(Parameter[] parameters, String name) {
        for (int i = 0; i < parameters.length; i++) {
            if (name.equals(parameters[i].getName())) {
                return i;
            }
        }
        return null;
    }
}
