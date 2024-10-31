package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 亚马逊请求站点IDS 类型
 *
 */
@Getter
@AllArgsConstructor
public enum AmazonMarketplaceIdsTypeEnum {

    DEFAULT("default", "默认:当前店铺站点"),
    NONE("none", "无:不指定任何站点"),
    ALL_AUTH("allAuth", "所有已授权的站点"),
    ;


    /**
     * 商城编号
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 名称
     */
    private final String name;

    /**
     * 获取当前对应类型
     */
    public static AmazonMarketplaceIdsTypeEnum getByType(String marketplaceIdsType) {
        return Arrays.stream(AmazonMarketplaceIdsTypeEnum.values())
                .filter(e-> e.getCode().equalsIgnoreCase(marketplaceIdsType))
                .findFirst()
                .orElse(DEFAULT);
    }
}
