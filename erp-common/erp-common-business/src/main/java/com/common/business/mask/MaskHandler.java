package com.common.business.mask;

/**
 * 脱敏处理器接口（每个 {@link MaskStrategy} 对应一个 Bean 实现）
 *
 * <p>由 Spring 自动收编为 {@code List<MaskHandler>}，
 * {@link com.common.business.mask.core.MaskCore} 启动时按 {@link #strategy()}
 * 建立 {@code Map<MaskStrategy, MaskHandler>} 调度表。</p>
 *
 * <p>实现要点：</p>
 * <ul>
 *   <li>{@link #handle(Object, MaskContext)} 必须是无副作用的：仅根据入参返回新值</li>
 *   <li>必须能处理 {@code null} / 空字符串 / 类型不匹配的情况，按需返回原值</li>
 *   <li>禁止在 handle 内访问数据库 / 远程调用 / 加锁；要求 O(1) ~ O(n) 字符串处理</li>
 * </ul>
 *
 * <p>业务方扩展自定义策略：实现本接口 + 注册为 Spring Bean，再在 {@link Mask#strategy()}
 * 或 {@code cfg_mask_field.strategy} 引用对应枚举值即可。
 * 若需新增策略类型，先在 {@link MaskStrategy} 添加新枚举值。</p>
 *
 * @author cloud-erp
 */
public interface MaskHandler {

    /**
     * 该处理器对应的策略枚举值
     */
    MaskStrategy strategy();

    /**
     * 对单个字段值执行脱敏
     *
     * @param value 字段原值（可能为 null / 非 String 类型）
     * @param ctx   单次处理上下文
     * @return 脱敏后的值；不命中或不处理时返回原 value
     */
    Object handle(Object value, MaskContext ctx);
}
