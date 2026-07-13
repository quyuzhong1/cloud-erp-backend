package com.common.business.aspect;

import com.common.business.annotation.DistributeLocker;
import io.seata.core.context.RootContext;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import io.seata.tm.api.transaction.TransactionHook;
import io.seata.tm.api.transaction.TransactionHookManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.redisson.RedissonMultiLock;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

        try (MockedStatic<GlobalTransactionContext> seataContext = mockStatic(GlobalTransactionContext.class);
             MockedConstruction<RedissonMultiLock> ignored = mockMultiLockConstruction()) {
            seataContext.when(GlobalTransactionContext::getCurrentOrCreate).thenReturn(mock(GlobalTransaction.class));

            Object result = aspect.doAround(joinPoint);

            assertEquals("ok", result);
            assertEquals(0, unlockCount.get());

            List<TransactionHook> hooks = TransactionHookManager.getHooks();
            assertEquals(1, hooks.size());
            hooks.get(0).afterCommit();

            assertEquals(1, unlockCount.get());
        }
    }

    @Test
    public void localSpringTransactionUnlocksAfterTransactionCompletion() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        try (MockedConstruction<RedissonMultiLock> ignored = mockMultiLockConstruction()) {
            Object result = aspect.doAround(joinPoint);

            assertEquals("ok", result);
            assertEquals(0, unlockCount.get());

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());
            synchronizations.get(0).afterCommit();
            synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

            assertEquals(1, unlockCount.get());
        }
    }

    @Test
    public void isCrossServiceSeataParticipant_returnsFalseWhenNotInGlobalTransaction() {
        assertFalse(invokeIsCrossServiceSeataParticipant());
    }

    @Test
    public void isCrossServiceSeataParticipant_returnsFalseWhenNoHttpContext() {
        RootContext.bind(XID);
        assertFalse(invokeIsCrossServiceSeataParticipant());
    }

    @Test
    public void isCrossServiceSeataParticipant_returnsFalseWhenHttpWithoutXidHeader() {
        RootContext.bind(XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertFalse(invokeIsCrossServiceSeataParticipant());
    }

    @Test
    public void isCrossServiceSeataParticipant_returnsTrueWhenFeignInboundXid() {
        RootContext.bind(XID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RootContext.KEY_XID, XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertTrue(invokeIsCrossServiceSeataParticipant());
    }

    @Test
    public void isCrossServiceSeataParticipant_returnsTrueWhenJudgementFails() {
        RootContext.bind(XID);
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenThrow(new IllegalStateException("simulate request access failure"));
        RequestContextHolder.setRequestAttributes(attrs);
        assertTrue(invokeIsCrossServiceSeataParticipant());
    }

    @Test
    public void crossServiceParticipantUnlocksWhenMethodEnds() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        RootContext.bind(XID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RootContext.KEY_XID, XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try (MockedConstruction<RedissonMultiLock> ignored = mockMultiLockConstruction()) {
            Object result = aspect.doAround(joinPoint);

            assertEquals("ok", result);
            assertTrue(TransactionHookManager.getHooks().isEmpty());
            assertEquals(1, unlockCount.get());
        }
    }

    private MockedConstruction<RedissonMultiLock> mockMultiLockConstruction() {
        return mockConstruction(RedissonMultiLock.class, (mock, context) -> {
            when(mock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
            doAnswer(invocation -> {
                unlockCount.incrementAndGet();
                return null;
            }).when(mock).unlock();
        });
    }

    private boolean invokeIsCrossServiceSeataParticipant() {
        return ReflectionTestUtils.invokeMethod(aspect, "isCrossServiceSeataParticipant");
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
        // keyName 使用 arg0：测试类未启用 -parameters，运行时参数名为 arg0 而非 id
        @DistributeLocker(businessType = "testLock", keyName = "arg0", unlockAfterTx = true)
        public String lockedBusiness(String id) {
            return id;
        }
    }
}
