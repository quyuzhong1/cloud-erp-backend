package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 亚马逊SP-API中使用的报告类型
 * <a href="https://developer-docs.amazon.com/sp-api/docs/inventory-reports-attributes">来源链接</a>
 *
 * @author Jim
 * @date 2023/10/17 10:13
 */
@Getter
@AllArgsConstructor
public enum AmazonReportRecordTypeEnum {
    // 库存报告类型值
    // https://developer-docs.amazon.com/sp-api/docs/report-type-values-inventory
    GET_FLAT_FILE_OPEN_LISTINGS_DATA("GET_FLAT_FILE_OPEN_LISTINGS_DATA","库存报告"),
    GET_MERCHANT_LISTINGS_ALL_DATA("GET_MERCHANT_LISTINGS_ALL_DATA","所有商品信息报告"),
    GET_MERCHANT_LISTINGS_DATA("GET_MERCHANT_LISTINGS_DATA","在售商品报告"),
    GET_MERCHANT_LISTINGS_INACTIVE_DATA("GET_MERCHANT_LISTINGS_INACTIVE_DATA","非在售商品报告"),
    GET_MERCHANT_LISTINGS_DATA_BACK_COMPAT("GET_MERCHANT_LISTINGS_DATA_BACK_COMPAT","表符分隔的库存模板文件在售商品报告"),
    GET_MERCHANT_LISTINGS_DATA_LITE("GET_MERCHANT_LISTINGS_DATA_LITE","在售商品报告精简版（仅包含数量大于零的商品的 SKU、ASIN、价格和数量字段）"),
    GET_MERCHANT_LISTINGS_DATA_LITER("GET_MERCHANT_LISTINGS_DATA_LITER","在售商品报告精简版（仅包含数量大于零的商品的 SKU 和数量字段。）"),
    GET_MERCHANT_CANCELLED_LISTINGS_DATA("GET_MERCHANT_CANCELLED_LISTINGS_DATA","已取消商品报告"),
    GET_MERCHANTS_LISTINGS_FYP_REPORT("GET_MERCHANTS_LISTINGS_FYP_REPORT","禁止显示商品报告"),
    GET_PAN_EU_OFFER_STATUS("GET_PAN_EU_OFFER_STATUS","欧洲整合服务资格：亚马逊物流 ASIN"),
    GET_MFN_PANEU_OFFER_STATUS("GET_MFN_PANEU_OFFER_STATUS","欧洲整合服务资格：自配送 ASIN"),
    GET_REFERRAL_FEE_PREVIEW_REPORT("GET_REFERRAL_FEE_PREVIEW_REPORT","销售佣金预览报告"),

    // 亚马逊物流 (FBA) 报告类型值
    // https://developer-docs.amazon.com/sp-api/docs/report-type-values-fba
    // 亚马逊物流库存报告
    GET_FBA_MYI_ALL_INVENTORY_DATA("GET_FBA_MYI_ALL_INVENTORY_DATA", "亚马逊物流管理库存 - 已存档"),
    GET_RESERVED_INVENTORY_DATA("GET_RESERVED_INVENTORY_DATA", "亚马逊物流预留库存报告"),
    GET_FBA_INVENTORY_PLANNING_DATA("GET_FBA_INVENTORY_PLANNING_DATA", "亚马逊物流管理库存状况报告"),



    ;

    /**
     * 商城编号
     */
    @EnumValue
    @JsonValue
    private final String recordType;

    /**
     * 名称
     */
    private final String name;


}
