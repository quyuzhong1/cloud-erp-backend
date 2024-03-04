package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 亚马逊SP-API Listing识别类型
 * <a href="https://developer-docs.amazon.com/sp-api/docs/catalog-items-api-v2022-04-01-reference#identifierstype">来源链接</a>
 *
 * @author Jim
 * @date 2023/11/1
 */
@Getter
@AllArgsConstructor
public enum AmazonIdentifiersTypeEnum {

    ASIN("ASIN",	"Amazon Standard Identification Number"),
    EAN("EAN",	"European Article Number"),
    GTIN("GTIN",	"Global Trade Item Number"),
    ISBN("ISBN",	"International Standard Book Number"),
    JAN("JAN",	"Japanese Article Number"),
    MINSAN("MINSAN",	"Minsan Code"),
    SKU("SKU",	"Stock Keeping Unit, a seller-specified identifier for an Amazon listing. Note: Must be accompanied by sellerId"),
    UPC("UPC",	"Universal Product Code"),

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

}
