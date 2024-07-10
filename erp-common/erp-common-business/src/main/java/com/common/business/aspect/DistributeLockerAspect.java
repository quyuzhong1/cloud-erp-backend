package com.common.business.aspect;

import com.common.business.annotation.DistributeLocker;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分布式锁切面实现
 * @since 2024/07/04
 * @author Edison.Qu
 * Edison.Qu 2024/07/04
 * <p>使用示例：</p>
 * <ul>
 * <font color="red">@DistributeLocker()</font>
 * <li>取id作为分布式锁的key</li>
 * <li>public void updateById(String id){...}</li>
 * </ul>
 * <ul>
 * <font color="red">@DistributeLocker(keyName="id,name")</font>
 * <li>取user实体id和name作为分布式锁的key</li>
 * <li>public void updateUser(SysUserEntity user){...}</li>
 * </ul>
 * <ul>
 * <font color="red">@DistributeLocker(argIndex=1,keyName="id")</font>
 * <li>取channel对象id作为分布式锁的key  </li>
 * <li>public void updateChannel(String sellerId, BasicChannelEntity channel){...}</li>
 * </ul>
 * <ul>
 * <font color="red">@DistributeLocker(keyName="id")</font>
 * <li>取channel对象id(集合)作为分布式锁的key集合(批量加锁),支持内嵌集合a->list->list2-..</li>
 * <li>public void updateChannel(List<BasicChannelEntity> channelList){...}</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
public class DistributeLockerAspect {
    @Resource
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.common.business.annotation.DistributeLocker)")
    public void dataPointCut() {
    }

    @Around("dataPointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Method method = currentMethod(pjp);
        //获取到方法的注解对象
        DistributeLocker annotation = method.getAnnotation(DistributeLocker.class);

        //当前线程名
        String threadName = Thread.currentThread().getName();
        log.info("线程{}------进入分布式锁aop------", threadName);

        List<String> keys = getLockKeys(annotation, pjp);

        // 获取锁
        List<RLock> rLocks;
        rLocks = keys.stream()
                .map(key -> redissonClient.getLock(key))
                .collect(Collectors.toList());

        RedissonMultiLock multiLock = new RedissonMultiLock(rLocks.toArray(new RLock[0]));
        // 尝试加锁
        try {
            boolean locked = multiLock.tryLock(annotation.waiteTime(), annotation.timeUnit());
            if(locked){
                log.info("线程{} 获取锁成功,key={}", threadName, keys);
                return pjp.proceed();
            } else {
                log.warn("线程{} 获取锁失败,key={}", threadName, keys);
                throw new RuntimeException("线程 "+threadName+" 获取锁失败,请求超时");
            }
        } catch (InterruptedException e) {
            log.error("线程{} 获取锁失败", threadName);
            throw new RuntimeException("线程 "+threadName+" 获取锁失败,请求超时",e);
        } finally {
            if(multiLock.isLocked()){
                try {
                    multiLock.unlock();
                    log.info("线程{} 释放锁成功,key={}", threadName,keys);
                }catch (Exception e){
                    log.error("线程"+threadName+"释放锁失败", e);
                }
            }
        }
    }

    // 生成锁Key
    private List<String> getLockKeys(DistributeLocker annotation, ProceedingJoinPoint pjp)  {
        List<String> result=new ArrayList<>();
        String className = getTargetClassName(pjp);
        String methodName = getTargetMethodName(pjp);
        String prefixStr= annotation.businessType().equals("")? className + "." + methodName: annotation.businessType();

        Object param = pjp.getArgs()[annotation.argIndex()];
        List<String> keys = getKeysByParam(annotation.keyName(), param);
        for (String key:keys){
             result.add("LOCK:" + prefixStr +"." + key);
        }
        return result;
    }

    private List<String> getKeysByParam(String keyFields, Object param) {
        List<String> result = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        if(param instanceof List){
            objects.addAll((List<?>)param);
        } else {
            objects.add(param);
        }

        for (Object obj : objects) {
            if(keyFields.equals("#")){
                result.add(obj.toString());
            } else {
                result.addAll(getKeysFromObject(obj, keyFields.split(",")));
            }
        }

        return result;
    }

    private List<String> getKeysFromObject(Object obj, String[] keyFields) {
        List<String> keys = new ArrayList<>();
        List<List<String>> fieldValuesList = new ArrayList<>();

        for (String fieldPath : keyFields) {
            fieldValuesList.add(getFieldValues(obj, fieldPath.trim().split("\\."), 0));
        }

        // 将各个字段路径末端的值用 | 连接起来
        int maxLength = fieldValuesList.stream().mapToInt(List::size).max().orElse(0);
        for (int i = 0; i < maxLength; i++) {
            StringBuilder combinedKey = new StringBuilder();
            for (List<String> fieldValues : fieldValuesList) {
                if (combinedKey.length() > 0) {
                    combinedKey.append("|");
                }
                combinedKey.append(i < fieldValues.size() ? fieldValues.get(i) : "");
            }
            keys.add(combinedKey.toString());
        }

        return keys;
    }

    /**
     * 获取对象的字段值
     * @param obj 对象
     * @param fieldPath 字段路径
     * @param index 序号
     * @return      字段值
     */
    private List<String> getFieldValues(Object obj, String[] fieldPath, int index) {
        List<String> results = new ArrayList<>();
        if (obj == null || index >= fieldPath.length) {
            return results;
        }

        String fieldName = fieldPath[index];
        Object value = getField(obj, fieldName);

        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                results.addAll(getFieldValues(item, fieldPath, index + 1));
            }
        } else if (index == fieldPath.length - 1) { // We are at the end of the field path
            results.add(value != null ? value.toString() : "");
        } else {
            results.addAll(getFieldValues(value, fieldPath, index + 1));
        }
        return results;
    }

    /**
     * 获取对象的字段
     * @param obj       对象
     * @param fieldName 字段名
     * @return          字段值
     */
    private Object getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
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
        Method resultMethod = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                resultMethod = method;
                break;
            }
        }
        return resultMethod;
    }

    private String getTargetMethodName(ProceedingJoinPoint pjp) {
        return ((MethodSignature) pjp.getSignature()).getMethod().getName();
    }

    private String getTargetClassName(ProceedingJoinPoint pjp) {
        StringBuilder classBuffer = new StringBuilder();
        String className = pjp.getTarget().getClass().getName();
        String[] clsNames = className.split("\\.");
        for (int i = 0; i < clsNames.length; i++) {
            if (i < clsNames.length - 1) {
                classBuffer.append(clsNames[i].charAt(0));
                classBuffer.append(".");
            } else {
                classBuffer.append(clsNames[i]);
            }
        }

        return classBuffer.toString();
    }
}
