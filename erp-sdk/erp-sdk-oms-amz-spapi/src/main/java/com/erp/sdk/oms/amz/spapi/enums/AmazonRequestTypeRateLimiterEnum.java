package com.erp.sdk.oms.amz.spapi.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.alibaba.fastjson.JSONObject;

/**
 * 亚马逊SP-API 请求速率默认配置
 *
 * @author Jim
 * @date 2023/12/25
 */
@Getter
@AllArgsConstructor
public enum AmazonRequestTypeRateLimiterEnum {

    // 订单相关
    ORDER_LIST("0.0167", "20", "order","订单列表"),
    ORDER_ITEMS("0.5", "30", "order_items","订单详情"),
    ORDER_ADDRESS("0.04512", "20", "order_address","订单地址"),
    BUYER_INFO("0.04512", "20", "buyer_info","买家信息"),
//    ORDER_ADDRESS("0.0167", "20", "order_address","订单地址"),

    // 商品相关
    PRODUCT_ITEMS("5", "5", "product","商品详情"),
    PRODUCT_PRICING("1", "1", "product_pricing","商品详情"),
    PRODUCT_LISTING("1", "1", "product_listing","商品列表"),

    // 货件相关
    FBA_SHIPMENT("2", "30","fba_shipment","货件信息"),
    FBA_SHIPMENT_DETAIL("2", "30","fba_shipment_detail","货件详情信息"),
    //履行订单
    FULFILL_ORDER("2", "30","fulfill_order","履行订单"),
    // 入库计划
    FBA_INBOUND_PLAN("2", "6","fba_inbound_plan","FBA入库计划列表"),
    FBA_INBOUND_PLAN_DETAIL("2", "6","fba_inbound_plan_detail","FBA入库计划信息"),
    FBA_INBOUND_PLAN_BOXES("2", "6","fba_inbound_plan_boxes","FBA入库计划装箱信息"),
    FBA_INBOUND_PLAN_SHIPMENT("2", "6","fba_inbound_plan_shipment","FBA入库计划货件信息"),
    // FBA库存
    FBA_INVENTORY("90", "150","fba_inventory","FBA库存"),

    // 报告相关
    REPORTS("0.0222", "10","reports","报告列表"),
    REPORTS_CREATE("0.0167", "15","reports_create","创建报告"),
    REPORTS_QUERY("2", "15","reports_query","根据ID查询报告"),
    REPORTS_DOCUMENT_QUERY("2", "15","reports_query","根据报告文档ID查询报告文档"),

    // 财务相关
    FINANCIAL_EVENTS("0.5", "30","financial_events","财务事件"),
    ;


    /**
     * rateLimit
     */
    @EnumValue
    @JsonValue
    private final String rateLimit;

    /**
     * burst
     */
    private final String burst;

    /**
     * 业务类型名称:
     * 来源枚举:不一定存在枚举
     * {@link com.common.business.enums.BusinessTypeEnum}
     */
    private final String businessTypeName;

    /**
     * 详情
     */
    private final String remark;

    public final static String requestTypeName = "requestTypeName";

    public final static String rateLimitName = "rateLimitName";

    public final static String burstName = "burstName";

    public final static String limitKey = "limitKey";


    public JSONObject getExtentJsonObj(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(requestTypeName, this.getBusinessTypeName());
        jsonObject.put(rateLimitName, this.getRateLimit());
        jsonObject.put(burstName, this.getBurst());
        return jsonObject;
    }
}
