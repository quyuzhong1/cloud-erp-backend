package com.erp.server.mrp.calculation.handler;

public interface SkuCalculationHandler<T> {

    default void handle(T t) {
        if (shouldHandle(t)) {
            // 处理当前逻辑
            if (doHandle(t)) {
                // 动态获取下一个处理器
                SkuCalculationHandler<T> nextHandler = getNextHandler(t);
                if (nextHandler != null) {
                    nextHandler.handle(t);
                }
            }
        } else {
            // 动态获取下一个处理器
            SkuCalculationHandler<T> nextHandler = getNextHandler(t);
            if (nextHandler != null) {
                nextHandler.handle(t);
            }
        }
    }

    /**
     * 获取下个责任链
     */
    SkuCalculationHandler<T> getNextHandler(T t);

    // 判断是否需要处理当前请求
    boolean shouldHandle(T t);

    /**
     * 执行具体的处理逻辑
     */
    boolean doHandle(T t);
}
