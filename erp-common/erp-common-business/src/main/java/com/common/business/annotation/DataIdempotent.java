package com.common.business.annotation;


import java.lang.annotation.*;

/**
 * 数据幂等性注解 支持参数标记
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataIdempotent {
    /**
     * 入参主键id的名称
     */
    String keyIdName() default "";

    /*** 上锁时长，默认设置时间 30秒
     *** @return
     **/
    long leaseTime() default -1L;

    /***
     * 尝试时间，设置时间内通过自旋一致尝试获取锁，
     * 默认 0秒
     * 通常时间要小于 leaseTime 时间**
     * @return
     * */
    long waitTime() default 0L;

    /**
     * 业务类型
     *
     * @return
     */
    String businessType() default "";
}
