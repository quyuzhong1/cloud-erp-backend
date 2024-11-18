package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @version 1.0
 * @description: B2C销售订单作废类型枚举
 * @date 2023/12/20 10:27
 */
@Getter
public enum SoB2cErrorTypeEnum {

    SUBMIT_DELIVERY("submitDelivery",  "提交发货异常"),
    SIGN_DELIVERY("signDelivery",  "标记发货异常"),
    GET_LOGISTICS_CODE("getLogisticsCode",  "获取物流单异常"),
    GENERATE_OUTSTOCK("generateOutstock",  "生成销售出库单"),
    INTERCEPT_SUCCESS("interceptSuccess",  "物流拦截成功"),
    ORDER_FORECAST("orderForecast",  "订单预报失败"),
    INSTOCK_FORECAST("instockForecast",  "入库预报失败"),
    CANCEL_ORDER_FORECAST("cancelOrderForecast",  "取消订单预报失败"),
    THIRD_WAREHOUSE_OUT_EXCEPTION("thirdWarehouseOutException",  "第三方仓出库异常"),
    GENERATE_TRANSFER_INFO("generateTransferInfo",  "生成直接调拨单"),
    VIRTUAL_FREEZE_QTY("virtualFreezeQty",  "扣减虚拟冻结库存"),
    ORDER_FETCH_FAIL("orderFetchFail",  "订单拉取失败"),
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


    SoB2cErrorTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cErrorTypeEnum typeEnum : SoB2cErrorTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }

    public static SoB2cErrorTypeEnum getEnum(String code) {
        for (SoB2cErrorTypeEnum typeEnum : SoB2cErrorTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
