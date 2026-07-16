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

/**
 * {@link DistributeLockerAspect} 解锁策略单测。
 * <p>覆盖发起方 / 参与方 / 纯本地事务等分支，以及 {@code isCrossServiceSeataParticipant} 判定逻辑。</p>
 */
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

    // ==================== 解锁策略：发起方 ====================

    /**
     * 发起方顶层全局事务：加锁时仅有 XID（inSeataTx=true），无活跃本地 Spring 事务。
     * 预期注册 Seata TransactionHook，全局事务提交后才解锁，方法返回时不解锁。
     */
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

    /**
     * 发起方嵌套调用：外层 @GlobalTransactional 已开启，加锁时 inSeataTx=true 且 inSpringTx=true，
     * 且非跨服务参与方。预期优先走 TransactionHook 而非 Spring TransactionSynchronization。
     */
    @Test
    public void launcherNestedGlobalTransactionPrefersSeataHookOverSpringSync() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        RootContext.bind(XID);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        try (MockedStatic<GlobalTransactionContext> seataContext = mockStatic(GlobalTransactionContext.class);
             MockedConstruction<RedissonMultiLock> ignored = mockMultiLockConstruction()) {
            seataContext.when(GlobalTransactionContext::getCurrentOrCreate).thenReturn(mock(GlobalTransaction.class));

            Object result = aspect.doAround(joinPoint);

            assertEquals("ok", result);
            assertEquals(0, unlockCount.get());
            assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());

            List<TransactionHook> hooks = TransactionHookManager.getHooks();
            assertEquals(1, hooks.size());
            hooks.get(0).afterCommit();

            assertEquals(1, unlockCount.get());
        }
    }

    // ==================== 解锁策略：纯本地 Spring 事务 ====================

    /**
     * 纯本地 @Transactional：无 Seata 全局事务，加锁时 inSpringTx=true。
     * 预期注册 TransactionSynchronization，事务提交/完成后才解锁。
     */
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

    // ==================== 参与方判定：isCrossServiceSeataParticipant ====================

    /** 未处于 Seata 全局事务 → 非参与方。 */
    @Test
    public void isCrossServiceSeataParticipant_returnsFalseWhenNotInGlobalTransaction() {
        assertFalse(invokeIsCrossServiceSeataParticipant());
    }

    /** 有 XID 但无 HTTP 上下文（如 XXL-JOB 发起方）→ 非参与方，仍可走 TransactionHook。 */
    @Test
    public void isCrossServiceSeataParticipant_returnsFalseWhenNoHttpContext() {
        RootContext.bind(XID);
        assertFalse(invokeIsCrossServiceSeataParticipant());
    }

    /** 有 HTTP 上下文但入站请求未携带 TX_XID → 本服务为发起方，非参与方。 */
    @Test
    public void isCrossServiceSeataParticipant_returnsFalseWhenHttpWithoutXidHeader() {
        RootContext.bind(XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertFalse(invokeIsCrossServiceSeataParticipant());
    }

    /** 入站 Feign 请求携带 TX_XID → 跨服务 Seata 参与方。 */
    @Test
    public void isCrossServiceSeataParticipant_returnsTrueWhenFeignInboundXid() {
        RootContext.bind(XID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RootContext.KEY_XID, XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertTrue(invokeIsCrossServiceSeataParticipant());
    }

    /** 读取入站 TX_XID 失败时保守按参与方处理，避免误注册 Hook 导致锁泄漏。 */
    @Test
    public void isCrossServiceSeataParticipant_returnsTrueWhenJudgementFails() {
        RootContext.bind(XID);
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenThrow(new IllegalStateException("simulate request access failure"));
        RequestContextHolder.setRequestAttributes(attrs);
        assertTrue(invokeIsCrossServiceSeataParticipant());
    }

    // ==================== 解锁策略：跨服务参与方 ====================

    /**
     * 参与方 + 无活跃本地 Spring 事务：TransactionHook 不会触发，退化为方法结束（finally）解锁。
     * 同方法 @Transactional 场景依赖内层切面先提交，再由 finally 释放。
     */
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

    /**
     * 参与方 + 加锁时已有活跃本地 Spring 事务：TransactionHook 不可用，
     * 改由 TransactionSynchronization 在本地事务提交/回滚后解锁。
     */
    @Test
    public void crossServiceParticipantUnlocksAfterLocalSpringTransaction() throws Throwable {
        joinPoint = newJoinPoint("lockedBusiness");
        RootContext.bind(XID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RootContext.KEY_XID, XID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        try (MockedConstruction<RedissonMultiLock> ignored = mockMultiLockConstruction()) {
            Object result = aspect.doAround(joinPoint);

            assertEquals("ok", result);
            assertTrue(TransactionHookManager.getHooks().isEmpty());
            assertEquals(0, unlockCount.get());

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());
            synchronizations.get(0).afterCommit();
            synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

            assertEquals(1, unlockCount.get());
        }
    }

    /** 统计 unlock 调用次数，用于断言解锁时机。 */
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
        /** keyName 使用 arg0：测试类未启用 -parameters，运行时参数名为 arg0 而非 id。 */
        @DistributeLocker(businessType = "testLock", keyName = "arg0", unlockAfterTx = true)
        public String lockedBusiness(String id) {
            return id;
        }
    }
}
