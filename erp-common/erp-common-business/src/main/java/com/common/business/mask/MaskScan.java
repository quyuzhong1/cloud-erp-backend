package com.common.business.mask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级脱敏开关注解
 *
 * <p>{@link com.common.business.mask.aspect.MaskAspect} 默认对所有
 * {@code com.erp.server.*.controller.api.*.*} 方法的返回值递归扫描脱敏，
 * 该注解用于「方法级覆盖」：</p>
 *
 * <ul>
 *   <li>{@link #disabled()} = true：该方法关闭脱敏（用于内部 / 超管接口 / 导出接口）</li>
 *   <li>{@link #permission()}：方法级默认权限码，被 {@link Mask#permission()} 字段级覆盖</li>
 * </ul>
 *
 * <p>不打该注解时按全局默认行为：扫描 + 仅超管看明文。</p>
 *
 * @author cloud-erp
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskScan {

    /**
     * 方法级默认看明文权限码。字段未单独指定 {@link Mask#permission()} 时继承该值。
     */
    String permission() default "";

    /**
     * 关闭该方法的脱敏（数据原样返回）。
     * 适用于：导出明文、内部对账、超管专属接口。
     */
    boolean disabled() default false;
}
