package com.erp.server.sys.service;

import com.erp.model.sys.dto.MaskDiagnoseDTO;

/**
 * 脱敏诊断服务：运维 / SRE 排查脱敏框架性能与配置的在线工具
 *
 * @author cloud-erp
 */
public interface MaskDiagnoseService {

    /**
     * 测试一条正则：过 ReDoS 黑名单 + 实测重复匹配耗时
     */
    MaskDiagnoseDTO.RegexTestVO testRegex(MaskDiagnoseDTO.RegexTestDTO dto);

    /**
     * 查看一个 POJO 类被脱敏框架解析后的字段描述符
     * <p>失败原因：类不存在 / ClassLoader 不可见 / 框架前缀类。</p>
     */
    MaskDiagnoseDTO.DescriptorVO describe(MaskDiagnoseDTO.DescriptorSearchDTO dto);

    /**
     * 当前 {@code MaskClassDescriptorRegistry} 缓存统计
     */
    MaskDiagnoseDTO.RegistryStatsVO stats();
}
