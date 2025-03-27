package com.common.business.aspect;

import com.common.business.annotation.DistributeLocker;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import java.lang.reflect.Parameter;
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
        // 1. 获取并验证当前方法
        Method method = currentMethod(pjp);
        if (method == null) {
            log.error("无法获取当前方法信息，请检查切入点配置");
            throw new IllegalArgumentException("当前方法不存在");
        }

        // 2. 获取注解和线程信息
        DistributeLocker annotation = method.getAnnotation(DistributeLocker.class);
        String threadName = Thread.currentThread().getName();
        String methodName = method.getName();

        log.info("线程[{}]执行方法[{}]准备获取分布式锁", threadName, methodName);

        // 3. 获取锁键并创建锁对象
        List<String> lockKeys = getLockKeys(annotation, pjp);
        if (lockKeys.isEmpty()) {
            log.warn("线程[{}]执行方法[{}]未找到需要锁定的键，执行方法不加锁", threadName, methodName);
            return pjp.proceed();
        }

        List<RLock> rLocks;
        rLocks = lockKeys.stream()
                .map(key -> redissonClient.getLock(key))
                .collect(Collectors.toList());

        // 4. 创建多锁并准备获取
        RedissonMultiLock multiLock = new RedissonMultiLock(rLocks.toArray(new RLock[0]));
        long waitTime = getWaitTime(annotation, pjp);
        boolean locked = false;

        try {
            // 5. 尝试获取锁
            locked = multiLock.tryLock(waitTime, annotation.timeUnit());
            if (!locked) {
                log.warn("线程[{}]执行方法[{}]获取锁失败，keys={}", threadName, methodName, lockKeys);
                throw new ServiceException(String.format("线程[%s]获取锁失败，请求超时", threadName));
            }

            log.info("线程[{}]执行方法[{}]获取锁成功，keys={}", threadName, methodName, lockKeys);
            // 6. 执行原方法
            return pjp.proceed();
        } catch (InterruptedException e) {
            log.error("线程[{}]执行方法[{}]获取锁时被中断", threadName, methodName, e);
            Thread.currentThread().interrupt(); // 重置中断状态
            throw new ServiceException(String.format("线程[%s]获取锁失败，请求被中断", threadName), e);
        } finally {
            // 7. 安全释放锁
            if (locked && multiLock.isHeldByCurrentThread()) {
                try {
                    multiLock.unlock();
                    log.info("线程[{}]执行方法[{}]释放锁成功，keys={}", threadName, methodName, lockKeys);
                } catch (Exception e) {
                    log.error("线程[{}]执��方法[{}]释放锁失败", threadName, methodName, e);
                }
            }
        }
    }

    /**
     * 获取等待时间
     * @param annotation    注解信息
     * @param pjp           切入点
     * @return              等待时间
     */
    private long getWaitTime(DistributeLocker annotation, ProceedingJoinPoint pjp) {
        List<Object> objList = getValuesByParam(pjp, annotation.waitTimeKey());
        if(!CollectionUtils.isEmpty(objList)){
            return Long.parseLong(objList.get(0).toString());
        }
        return annotation.waiteTime();
    }

    /**
     * 获取锁的key
     * @param annotation   注解信息
     * @param pjp          切入点
     * @return             锁的key
     */
    private List<String> getLockKeys(DistributeLocker annotation, ProceedingJoinPoint pjp)  {
        List<String> result=new ArrayList<>();
        String className = getTargetClassName(pjp);
        String methodName = getTargetMethodName(pjp);
        String prefixStr= annotation.businessType().isEmpty() ? className + "." + methodName: annotation.businessType();

        List<Object> keys = getValuesByParam(pjp, annotation.keyName());
        for (Object key:keys){
            result.add("RedissonLock:" + prefixStr +"." + key);
        }
        return result;
    }

    /**
     * 获取参数的索引
     * @param pjp       切入点
     * @param argName   参数名
     * @return          参数索引
     */
    private Integer getArgIndex(ProceedingJoinPoint pjp, String argName) {
        Method method = currentMethod(pjp);
        // 校验 method 是否为 null
        if (method == null) {
            log.error("无法获取方法信息，请检查切入点配置");
            throw new IllegalArgumentException("当前方法不存在");
        }
        //获取到方法的注解对象
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (argName.equals(parameters[i].getName())) {
                return i;
            }
        }
        throw new RuntimeException("参数名【"+ argName +"】不存在");
    }

    /**
     * 获取参数的值-通过参数名
     * @param pjp           切入点
     * @param keyFields     参数名
     * @return              参数值
     */
    private List<Object> getValuesByParam(ProceedingJoinPoint pjp, String keyFields) {
        List<Object> result = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        String argName = keyFields.split(",")[0].split("\\.")[0];
        if("".equals(argName)){
            return result;
        }

        Object param = null==pjp.getArgs()? null: pjp.getArgs()[getArgIndex(pjp, argName)];
        // 去掉第一个参数
        keyFields = keyFields.replaceAll(argName + "\\.", "");


        if(param instanceof List){
            objects.addAll((List<?>)param);
        } else {
            objects.add(param);
        }

        for (Object obj : objects) {
            if ("#".equals(keyFields) || keyFields.isEmpty()) {
                result.add(obj.toString());
            } else if(isStandardJavaType(obj)){
                // 如果是标准Java类型
                result.add(obj.toString());
            }else {
                result.addAll(getValuesFromObject(obj, keyFields.split(",")));
            }
        }

        return result;
    }

    /**
     * 判断是否为标准Java类型
     * @param obj   对象
     * @return      是否为标准Java类型
     */
    private boolean isStandardJavaType(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> clazz = obj.getClass();
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class;
    }

    /**
     * 获取对象的字段值
     * @param obj           对象
     * @param keyFields     字段名（多个字段用逗号分隔）
     * @return              字段值
     */
    private List<Object> getValuesFromObject(Object obj, String[] keyFields) {
        List<Object> keys = new ArrayList<>();
        List<List<Object>> fieldValuesList = new ArrayList<>();

        for (String fieldPath : keyFields) {
            fieldValuesList.add(getValuesFromField(obj, fieldPath.trim().split("\\."), 0));
        }

        // 将各个字段路径末端的值用 | 连接起来
        int maxLength = fieldValuesList.stream().mapToInt(List::size).max().orElse(0);
        for (int i = 0; i < maxLength; i++) {
            StringBuilder combinedKey = new StringBuilder();
            for (List<Object> fieldValues : fieldValuesList) {
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
    private List<Object> getValuesFromField(Object obj, String[] fieldPath, int index) {
        List<Object> results = new ArrayList<>();
        if (obj == null) {
            return results;
        }
        if(index >= fieldPath.length){
            results.add(obj);
            return results;
        }

        String fieldName = fieldPath[index];
        Object value = getField(obj, fieldName);

        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                results.addAll(getValuesFromField(item, fieldPath, index + 1));
            }
        } else if (index == fieldPath.length - 1) {
            // We are at the end of the field path
            results.add(value != null ? value.toString() : "");
        } else {
            results.addAll(getValuesFromField(value, fieldPath, index + 1));
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
            log.debug("获取字段{}值失败: {}", fieldName, e.getMessage());
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
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        log.warn("未找到方法：{}", methodName);
        // 返回 null 并记录警告日志
        return null;
    }

    /**
     * 获取目标方法名称
     * @param pjp   切入点
     * @return      方法名称
     */
    private String getTargetMethodName(ProceedingJoinPoint pjp) {
        return ((MethodSignature) pjp.getSignature()).getMethod().getName();
    }

    /**
     * 获取目标类名称
     * @param pjp   切入点
     * @return      类名称
     */
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