package com.common.business.mask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段级脱敏注解
 *
 * <p>标注在 DTO/VO/Entity 字段上，由 {@link com.common.business.mask.aspect.MaskAspect}
 * 在 Controller 出参返回时，递归扫描并按 {@link #strategy()} 指定的策略对值脱敏。</p>
 *
 * <p>触发链路：</p>
 * <pre>
 *   Controller 出参 → MaskAspect 切面 → MaskCore 递归扫描容器（ApiResult/IPage/PagingVO/List/Map）
 *                  → MaskClassDescriptorRegistry 获取类元数据
 *                  → 按字段 strategy 分发到 {@link MaskHandler}
 *                  → 写回字段值
 * </pre>
 *
 * <p>与 {@code cfg_mask_field} 配置表的合并规则：</p>
 * <ul>
 *   <li>同一 {@code (类全名, 字段名)} 配置表存在记录 → 配置表覆盖注解</li>
 *   <li>仅注解存在 → 用注解</li>
 *   <li>仅配置表存在 → 用配置表</li>
 *   <li>都不存在 → 不脱</li>
 * </ul>
 *
 * <p>权限码合并规则（决定哪些用户看明文）：</p>
 * <ul>
 *   <li>字段级 {@link #permission()} 不为空 → 用字段级</li>
 *   <li>否则继承方法级 {@link MaskScan#permission()}</li>
 *   <li>都为空 → 仅超管（{@code LoginUser.isSupper}）看明文</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mask {

    /**
     * 脱敏策略，默认 {@link MaskStrategy#AUTO_FROM_CONFIG}（仅配置表生效）。
     * 一般场景：直接指定 PHONE / ID_CARD / EMAIL / AMOUNT 等内置策略。
     */
    MaskStrategy strategy() default MaskStrategy.AUTO_FROM_CONFIG;

    /**
     * 自定义正则；仅 {@link MaskStrategy#CUSTOM} 时使用。
     * 命中部分将被 {@link #replacement()} 替换。
     */
    String regex() default "";

    /**
     * 自定义替换串；仅 {@link MaskStrategy#CUSTOM} 时使用。
     * 默认 ***
     */
    String replacement() default "***";

    /**
     * 看明文所需权限码。
     * <ul>
     *   <li>非空且当前 {@code LoginUser.permissionList} 包含该码 → 不脱敏</li>
     *   <li>空字符串 → 继承方法级 {@link MaskScan#permission()}</li>
     * </ul>
     */
    String permission() default "";

    /**
     * 是否对集合元素 / 嵌套对象递归脱敏。
     * 一般保持默认 true；仅在已知字段值是不可分割的字符串（如序列化 JSON 不希望被字段反射处理）时设为 false。
     */
    boolean recursive() default true;

    /**
     * 字段为 {@code null} 或空字符串时是否保持原样。
     * <ul>
     *   <li>true（默认）：null/empty 直接返回，不替换为 mask</li>
     *   <li>false：null/empty 也强制替换为 mask 占位</li>
     * </ul>
     */
    boolean keepEmpty() default true;

    /**
     * "不可见"语义开关：当用户无 {@link #permission()} 看明文权限时：
     * <ul>
     *   <li>false（默认）：按 {@link #strategy()} 脱敏展示（如 138****5678）</li>
     *   <li>true：脱敏后再把整字段置 null（前端看不到该字段）</li>
     * </ul>
     * <p>同 {@code cfg_mask_field.hide_when_masked}，配置表覆盖注解。
     * 有明文权限的用户始终看明文，本开关不参与。</p>
     */
    boolean hideWhenMasked() default false;
}
