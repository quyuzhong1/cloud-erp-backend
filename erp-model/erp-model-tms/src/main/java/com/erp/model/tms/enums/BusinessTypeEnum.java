package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum BusinessTypeEnum implements EnumMessage {
    CREATE_ORDER("createOrder", "创建订单"),
    CONFIRM_ORDER("confirmOrder", "确认订单"),
    UPDATE_ORDER("updateOrder", "更新订单"),
    INTERCEPT_ORDER("interceptOrder", "拦截订单"),
    QUERY_ORDER("queryOrder", "查询订单"),
    GET_LABEL("getLabel", "获取标签"),
    GET_TRACK("getTrack", "轨迹查询"),
    REGISTER_TRACK("registerTrack", "注册物流单"),
    GET_LABEL_LIST("getLabelList", "批量获取标签"),
    GET_CHANEL_LIST("getChanelList", "批量渠道列表"),
    UPDATE_WEIGHT("updateWeight", "更新重量"),
    CANCEL_ORDER("cancelOrder", "取消订单"),
    SHIPPING_PARAMETER("shippingParameter", "获取标记发货参数"),
    SHIPPING_ORDER("shippingOrder", "标记订单发货"),
    SHIPPING_DOCUMENT_PARAMETER("shippingDocumentParameter", "获取发货面单参数"),
    CREATE_SHIPPING_DOCUMENT("createShippingDocument", "创建发货面单"),
    SHIPPING_DOCUMENT_RESULT("shippingDocumentResult", "获取发货面单结果"),
    DOWNLOAD_SHIPPING_DOCUMENT("downloadShippingDocument", "下载发货面单"),
    GET_TRACK_NUMBER("getTrackNumber", "获取物流单跟踪号"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    BusinessTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (BusinessTypeEnum typeEnums : BusinessTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
