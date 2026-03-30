
package com.sdk.oms.temu.enums;

import com.common.business.constant.MongoTableNameContant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * temu枚举
 *
 */
@Getter
@AllArgsConstructor
public enum TemuEnum {

    US("US", "美国",  "http://40.118.250.12:7000/openapi/router"),
    NA("NA", "美国",  "http://40.118.250.12:7000/openapi/router"),
    EU("EU", "欧区",  "http://40.118.250.12:7001/openapi/router"),
    GLOBAL("GLOBAL", "全球（除美国，欧区）",  "http://40.118.250.12:7002/openapi/router"),
    ;

    /**
     * 区域
     */
    private final String code;

    private final String name;

    private final String url;

    public static TemuEnum getByCode(String code) {
        return Stream.of(TemuEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

}
