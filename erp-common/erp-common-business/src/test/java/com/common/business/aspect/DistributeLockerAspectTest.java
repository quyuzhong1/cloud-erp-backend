package com.common.business.aspect;

import com.common.business.annotation.DistributeLocker;
import io.seata.core.context.RootContext;
import io.seata.tm.api.transaction.TransactionHook;
import io.seata.tm.api.transaction.TransactionHookManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DistributeLockerAspectTest {

    private static final String XID = "127.0.0.1:8091:123456";

    private RedissonClient redissonClient;
    private RLock lock;
    private ProceedingJoinPoint joinPoint;
    private DistributeLockerAspect aspect;
    private AtomicInteger unlockCount;
    private final TargetService targetService = new TargetService();

    @Before
    public void setUp() throws Exception {
        unlockCount = new AtomicInteger();
        lock = newLock();
        redissonClient = newRedissonClient(lock);
        aspect = new DistributeLockerAspect();
        ReflectionTestUtils.setField(aspect, "redissonClient", redissonClient);
    }

    @After
    public void tearDown() {
        TransactionHookManager.clear();
        RequestContextHolder.resetRequestAttributes();
        if (RootContext.inGlobalTransaction()) {
            RootContext.unbind();
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    public void launcherGlobalTransactionUnlocksAfterSeataHook() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        RootContext.bind(XID);

        Object result = aspect.doAround(joinPoint);

        assertEquals("ok", result);
        assertEquals(0, unlockCount.get());

        List<TransactionHook> hooks = TransactionHookManager.getHooks();
        assertEquals(1, hooks.size());
        hooks.get(0).afterCommit();

        assertEquals(1, unlockCount.get());
    }

    @Test
    public void localSpringTransactionUnlocksAfterTransactionCompletion() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        Object result = aspect.doAround(joinPoint);

        assertEquals("ok", result);
        assertEquals(0, unlockCount.get());

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCommit();
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

        assertEquals(1, unlockCount.get());
    }

    @Test
    public void crossServiceParticipantUnlocksWhenMethodEnds() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        RootContext.bind(XID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RootContext.KEY_XID, XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Object result = aspect.doAround(joinPoint);

        assertEquals("ok", result);
        assertTrue(TransactionHookManager.getHooks().isEmpty());
        assertEquals(1, unlockCount.get());
    }

    private ProceedingJoinPoint newJoinPoint(String methodName) throws NoSuchMethodException {
        Method method = TargetService.class.getMethod(methodName, String.class);
        MethodSignature signature = (MethodSignature) Proxy.newProxyInstance(
                MethodSignature.class.getClassLoader(),
                new Class[]{MethodSignature.class},
                (proxy, invokedMethod, args) -> {
                    switch (invokedMethod.getName()) {
                        case "getMethod":
                            return method;
                        case "getName":
                            return methodName;
                        case "getParameterTypes":
                            return method.getParameterTypes();
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                });
        return (ProceedingJoinPoint) Proxy.newProxyInstance(
                ProceedingJoinPoint.class.getClassLoader(),
                new Class[]{ProceedingJoinPoint.class},
                (proxy, invokedMethod, args) -> {
                    switch (invokedMethod.getName()) {
                        case "getTarget":
                            return targetService;
                        case "getSignature":
                            return signature;
                        case "getArgs":
                            return new Object[]{"BILL-001"};
                        case "proceed":
                            return "ok";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                });
    }

    private RedissonClient newRedissonClient(RLock lock) {
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class[]{RedissonClient.class},
                (proxy, method, args) -> {
                    if ("getLock".equals(method.getName())) {
                        return lock;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private RLock newLock() {
        return (RLock) Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class[]{RLock.class},
                (proxy, method, args) -> {
                    if ("tryLock".equals(method.getName())) {
                        return true;
                    }
                    if ("unlock".equals(method.getName())) {
                        unlockCount.incrementAndGet();
                        return null;
                    }
                    if ("isHeldByCurrentThread".equals(method.getName())) {
                        return true;
                    }
                    if ("getName".equals(method.getName())) {
                        return "testLock";
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == Void.TYPE) {
            return null;
        }
        if (returnType == Boolean.TYPE) {
            return false;
        }
        if (returnType == Byte.TYPE) {
            return (byte) 0;
        }
        if (returnType == Short.TYPE) {
            return (short) 0;
        }
        if (returnType == Integer.TYPE) {
            return 0;
        }
        if (returnType == Long.TYPE) {
            return 0L;
        }
        if (returnType == Float.TYPE) {
            return 0F;
        }
        if (returnType == Double.TYPE) {
            return 0D;
        }
        if (returnType == Character.TYPE) {
            return '\0';
        }
        return null;
    }

    public static class TargetService {
        @DistributeLocker(businessType = "testLock", keyName = "id", unlockAfterTx = true)
        public String lockedBusiness(String id) {
            return id;
        }
    }
}
