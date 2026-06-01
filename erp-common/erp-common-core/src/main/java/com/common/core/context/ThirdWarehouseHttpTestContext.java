package com.common.core.context;

/**
 * 三方仓 HTTP 调用测试上下文。
 */
public class ThirdWarehouseHttpTestContext {

    private static final ThreadLocal<Boolean> TIMEOUT_TEST = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ThirdWarehouseHttpTestContext() {
    }

    public static void setTimeoutTest(Boolean timeoutTest) {
        TIMEOUT_TEST.set(Boolean.TRUE.equals(timeoutTest));
    }

    public static boolean isTimeoutTest() {
        return Boolean.TRUE.equals(TIMEOUT_TEST.get());
    }

    public static void remove() {
        TIMEOUT_TEST.remove();
    }
}
