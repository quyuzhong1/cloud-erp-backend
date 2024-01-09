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
//    ORDER_ADDRESS("0.0167", "20", "order_address","订单地址"),

    // 商品相关
    LISTING_ITEMS("5", "5", "order_items","订单详情"),
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
