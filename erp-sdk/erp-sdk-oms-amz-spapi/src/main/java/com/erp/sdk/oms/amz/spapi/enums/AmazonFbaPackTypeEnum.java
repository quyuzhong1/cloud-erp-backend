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

}
