package com.common.core.utils;

/**
 * @Classname: VoidFunc
 * @Description: TODO
 * @CreateTime: 2023-04-21  14:37
 * @Author: zhangchunlin
 */
@FunctionalInterface
public interface VoidFunc {

    void call() throws Exception;

    default void callWithRuntimeException() {
        try {
            call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
