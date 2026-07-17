package com.common.business.mask;

import com.common.business.mask.core.MaskCore;
import com.common.business.mask.core.MaskFieldDescriptor;
import com.common.business.vo.LoginUser;

/**
 * 字段脱敏豁免判定 SPI（扩展点）
 *
 * <p>当前脱敏框架内置两类豁免：</p>
 * <ol>
 *   <li>方法级 {@link MaskScan#disabled()} = true，整方法跳脱</li>
 *   <li>字段级 {@link Mask#permission()}（含 {@code cfg_mask_field.permission_code}）
 *       —— 当前用户 {@link LoginUser#getPermissionList()} 包含该权限码即看明文</li>
 * </ol>
 *
 * <p>本 SPI 用于<b>扩展第三种豁免判定</b>：业务侧实现并注册为 Spring Bean 后，
 * {@link MaskCore} 在「字段已配置策略 + 权限码兜底未命中」时会再调用本接口，
 * 返回 {@code true} 即看明文，{@code false} 走原 strategy 脱敏。</p>
 *
 * <h3>典型扩展场景</h3>
 * <ul>
 *   <li>未来字段权限框架（sys_field_resource + sys_field_binding + sys_field_grant）：
 *       业务侧实现本接口查询 grant 缓存，按主体合并算法判断是否豁免</li>
 *   <li>按数据所有者放开明文：owner 是当前 user 时不脱（少见，常规走数据权限）</li>
 *   <li>按时段放开：审批通过后注入临时豁免</li>
 * </ul>
 *
 * <h3>无实现时的默认行为</h3>
 * <p>项目里若无任何 {@code MaskPermissionEvaluator} Bean，{@link MaskCore} 完全等同当前行为。
 * 接口只在「至少一个 Bean 注册」时被调用，对现有逻辑零侵入。</p>
 *
 * <h3>实现注意</h3>
 * <ul>
 *   <li>方法在<b>每条数据每个 @Mask 字段</b>调用一次，必须 O(1) 或带本地缓存，
 *       禁止做远程 RPC / SQL 查询；建议提前预热全量配置到 {@code volatile Map} 快照</li>
 *   <li>抛异常会被 {@link MaskCore} 吞掉并按"不豁免（即脱敏）"处理，不影响响应</li>
 *   <li>多实现会按 Spring 默认顺序逐个调用，任意一个返回 {@code true} 即豁免</li>
 * </ul>
 *
 * @author cloud-erp
 */
public interface MaskPermissionEvaluator {

    /**
     * 判定当前用户对该字段是否看明文（豁免脱敏）。
     *
     * @param user  当前登录用户；可能为 {@code null}（未登录或上下文丢失）
     * @param owner 字段所在 POJO 实例（含其他字段值，方便实现"按 owner.createUserId 判断"等场景）；
     *              不能为 {@code null}
     * @param fd    字段元数据（含 strategy / regex / permission / 反射 Field）；不能为 {@code null}
     * @return {@code true} 表示该字段对该用户显示明文（不执行 strategy）；
     *         {@code false} 表示按 strategy 脱敏
     */
    boolean shouldShowPlain(LoginUser user, Object owner, MaskFieldDescriptor fd);
}
