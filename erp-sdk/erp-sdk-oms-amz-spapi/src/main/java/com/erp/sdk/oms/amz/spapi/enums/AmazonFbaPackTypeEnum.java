package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 亚马逊SP-API FBA货件包装类型
 * <p>
 * 对应平台 {@code areCasesRequired} 字段：
 * <ul>
 *     <li>{@code true}  -&gt; {@link #CASE_PACKED} 原厂包装</li>
 *     <li>{@code false} -&gt; {@link #INDIVIDUAL} 混装</li>
 * </ul>
 *
 * @author Jim
 */
@Getter
@AllArgsConstructor
public enum AmazonFbaPackTypeEnum {

    CASE_PACKED("CASE_PACKED", "原厂包装"),
    INDIVIDUAL("INDIVIDUAL", "混装"),

    ;

    /**
     * 编码
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 名称
     */
    private final String name;

    /**
     * 根据 {@code areCasesRequired} 解析包装类型
     *
     * @param areCasesRequired 是否需要原厂整箱
     * @return 包装类型枚举，入参为 {@code null} 时返回 {@code null}
     */
    public static AmazonFbaPackTypeEnum fromAreCasesRequired(Boolean areCasesRequired) {
        if (areCasesRequired == null) {
            return null;
        }
        return areCasesRequired ? CASE_PACKED : INDIVIDUAL;
    }

    /**
     * 兼容读：把任意原始值（枚举 code / 中文 name / 历史落库的中文）统一翻译成中文展示名。
     * <ul>
     *     <li>命中 code 或 name → 返回 {@link #name}（"原厂包装" / "混装"）</li>
     *     <li>无法识别（含 {@code null} / 空字符串） → 原样返回</li>
     * </ul>
     * 用于规避 packType 落库切换 code 后，已有中文存量数据导致前端展示与新数据风格不一致的问题。
     */
    public static String toDisplayName(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            return rawValue;
        }
        for (AmazonFbaPackTypeEnum item : values()) {
            if (item.code.equals(rawValue) || item.name.equals(rawValue)) {
                return item.name;
            }
        }
        return rawValue;
    }

    /**
     * 兼容读：把任意原始值（枚举 code / 中文 name / 历史落库的中文）统一归一化成 code。
     * <ul>
     *     <li>命中 code 或 name → 返回 {@link #code}（CASE_PACKED / INDIVIDUAL）</li>
     *     <li>无法识别（含 {@code null} / 空字符串） → 原样返回</li>
     * </ul>
     * 用于读取/对比时对历史中文与新 code 做归一化，避免按字符串精确匹配的查询/对账偏差。
     */
    public static String toCode(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            return rawValue;
        }
        for (AmazonFbaPackTypeEnum item : values()) {
            if (item.code.equals(rawValue) || item.name.equals(rawValue)) {
                return item.code;
            }
        }
        return rawValue;
    }

}
