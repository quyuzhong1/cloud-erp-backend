package com.erp.server.sys.service;

import java.util.Map;

/**
 * 脱敏规则配置"定时扫表 + 整表 hash 比对"服务
 *
 * <p>定位：{@code CfgMaskFieldService} / {@code CfgMaskWordService} 现有"service 写完即延迟双删"机制
 * 的<b>兜底层</b>，覆盖运维直接 SQL 改库 / 迁移脚本 / 跨服务直接写 cfg 表等绕过 service 的写入路径。</p>
 *
 * <h3>对账流程</h3>
 * <ol>
 *   <li>扫两张 cfg 表算整表 hash</li>
 *   <li>比对内存里的上次 hash 快照</li>
 *   <li>发现变化时调对应 service 的 {@code publishFullCache()} 触发"重算 + 回填 Redis"</li>
 *   <li>更新快照</li>
 * </ol>
 *
 * <h3>责任边界</h3>
 * <ul>
 *   <li><b>只管脱敏规则</b>（cfg_mask_field / cfg_mask_word）。</li>
 *   <li>权限失效（sys_user_role / sys_role_menu / sys_user_info）走另一套机制
 *       （service 层 publisher + Redis Pub/Sub + 60s TTL），<b>不在此服务责任范围</b>。</li>
 * </ul>
 *
 * @author cloud-erp
 */
public interface MaskCfgSyncService {

    /**
     * 执行一次同步：扫两张 cfg 表 → diff → 调 publishFullCache 回填 Redis。XXL-JOB 调用入口。
     *
     * <p>线程安全要求：本方法应该单线程跑（XXL-JOB 默认串行执行，路由策略建议 FIRST 单节点）。
     * 多线程并发会导致快照状态错乱。</p>
     *
     * @return 本次同步的统计信息（hash 是否变化、耗时、错误信息等）
     */
    Map<String, Object> runSync();

    /**
     * 当前内存快照视图（诊断用）：含 hash 短串 + 依赖可用性 + 最近一次任务统计
     */
    Map<String, Object> snapshotView();

    /**
     * 重置内存快照
     *
     * <p>用于诊断：把 firstRun 置回，下一次扫描重新填充快照不刷新 Redis；之后开始正常 diff。
     * 运维用于"怀疑快照脏了，想重新对账"的场景。</p>
     */
    void resetSnapshot();
}
