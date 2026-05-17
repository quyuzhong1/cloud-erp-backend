package com.erp.server.sys.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 脱敏规则配置"定时扫表 + 整表 hash 比对"专用 Mapper
 *
 * <p>覆盖 {@code cfg_mask_field} / {@code cfg_mask_word} 两张配置表，用整表 hash 判断是否变化。
 * 通过对账方式兜底"运维直接 SQL 改库 / 迁移脚本 / 跨服务直接写表"等绕过 service 的写入路径。</p>
 *
 * <h3>责任边界</h3>
 * <ul>
 *   <li><b>只管脱敏规则</b>：cfg_mask_field / cfg_mask_word。</li>
 *   <li>权限失效（sys_user_role / sys_role_menu / sys_user_info）<b>不在此 mapper 责任范围</b>，
 *       由 service 层 publisher + Redis Pub/Sub 即时机制 + FeignMaskPermissionResolver 本地 TTL 三层兜底。</li>
 * </ul>
 *
 * <p>SQL 实现见 {@code src/main/resources/mapper/MaskCfgSyncMapper.xml}。</p>
 *
 * @author cloud-erp
 */
@Mapper
public interface MaskCfgSyncMapper {

    /**
     * 整表算 cfg_mask_field 的 hash
     *
     * <p>只覆盖<b>影响脱敏行为</b>的列：class_path / field_name / strategy / custom_regex /
     * custom_replace / permission_code / hide_when_masked / enabled。
     * remark 等无关字段改动不会触发 publishFullCache。</p>
     *
     * <p>空表返回空字符串而不是 null，便于上层用 {@code Objects.equals} 比对。</p>
     */
    String scanCfgMaskFieldHash();

    /**
     * 整表算 cfg_mask_word 的 hash
     *
     * <p>只覆盖<b>影响脱敏行为</b>的列：word_type / word / enabled。
     * category / remark 等业务标记不参与，避免运营调标签时误刷词典。</p>
     */
    String scanCfgMaskWordHash();
}
