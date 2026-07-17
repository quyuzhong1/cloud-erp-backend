package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;

/**
 * 脱敏诊断接口请求 / 响应实体
 *
 * <p>提供给运维 / SRE 排查脱敏框架问题用：</p>
 * <ol>
 *   <li>{@link RegexTestDTO} → {@link RegexTestVO}：测试一条正则的 ReDoS 安全性 + 实际匹配耗时</li>
 *   <li>{@link DescriptorSearchDTO} → {@link DescriptorVO}：查看一个 POJO 类被脱敏框架解析后的字段描述符</li>
 *   <li>{@link RegistryStatsVO}：当前 {@code MaskClassDescriptorRegistry} 缓存统计</li>
 * </ol>
 *
 * <p><b>权限</b>：诊断接口须限制为运维 / 超管访问（通过菜单权限码挂菜单上控制）。</p>
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class MaskDiagnoseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Data
    @NoArgsConstructor
    public static class RegexTestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "regex 不能为空")
        private String regex;

        @NotBlank(message = "sampleText 不能为空")
        private String sampleText;

        private String replacement = "***";

        /**
         * 重复匹配次数，取平均消除 JIT 抖动；默认 100。
         * <p>上限 10000 保护服务（更大没意义，本接口主要看相对耗时）。</p>
         */
        @Min(value = 1, message = "repeat 最小为 1")
        @Max(value = 10000, message = "repeat 最大为 10000")
        private Integer repeat = 100;
    }

    @Data
    @NoArgsConstructor
    public static class RegexTestVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** RegexSafetyGuard 静态分析是否通过 */
        private boolean safe;

        /** safe=false 时的拒绝原因 */
        private String reason;

        /** 是否编译成功（语法正确）；safe=false 时不会编译 */
        private boolean compiled;

        /** 重复 repeat 次的总耗时（微秒） */
        private Long totalCostUs;

        /** 单次平均耗时（微秒） */
        private Long avgCostUs;

        /** 实际重复次数（与请求 repeat 一致） */
        private Integer repeat;

        /** 单次匹配次数（matcher.find() 命中数） */
        private Integer matchCount;

        /** 脱敏后的样本输出（前 256 字符截断） */
        private String maskedPreview;

        /** 输入文本长度（字符） */
        private Integer inputLen;
    }

    @Data
    @NoArgsConstructor
    public static class DescriptorSearchDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "classPath 不能为空")
        private String classPath;
    }

    @Data
    @NoArgsConstructor
    public static class DescriptorVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String classPath;

        /** 该类是否被识别为"无脱敏字段"（命中 NO_MASK 哨兵） */
        private boolean noMask;

        /** 被框架识别为需要处理的字段总数（含 container 嵌套字段） */
        private Integer totalFields;

        private List<FieldVO> fields;
    }

    @Data
    @NoArgsConstructor
    public static class FieldVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String fieldName;
        private String fieldType;
        private String strategy;
        private String regex;
        private String replacement;
        private String permission;
        private boolean keepEmpty;
        private boolean recursive;
        private boolean hideWhenMasked;
        private int sort;
        /** 是否仅作为递归容器（无显式策略） */
        private boolean container;
    }

    @Data
    @NoArgsConstructor
    public static class RegistryStatsVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 已扫描类数 */
        private Integer scannedClasses;

        /** 已扫描类全限定名（按字母序，便于排查） */
        private List<String> classNames;
    }
}
