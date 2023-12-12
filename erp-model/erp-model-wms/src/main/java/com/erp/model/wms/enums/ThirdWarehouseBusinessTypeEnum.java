package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum ThirdWarehouseBusinessTypeEnum implements EnumMessage {
    AUTH("auth", "授权"),
    GET_SKU("getSku", "产品数据拉取"),
    GET_WAREHOUSE("getWarehouse", "仓库数据拉取"),
    GET_BASE_ADDRESS("getBaseAddress", "地址基础信息拉取"),
    GET_INBOUND_RECEIPT("getInboundReceipt", "入库单签收数据获取"),
    GET_TRANSIT_WAREHOUSE_AND_LOGISTIC("getTransitWarehouseAndLogistic", "中转仓及支持的物流产品基础数据获取"),
    CREATE_INBOUND_BILL("createInboundBill", "入库单创建接口对接"),
    CANCEL_INBOUND_BILL("cancelInboundBill", "入库单取消接口对接"),
    CREATE_OUTBOUND_BILL("createOutboundBill", "订单发货对接海外仓出库创建接口"),
    CANCEL_OUTBOUND_BILL("cancelOutboundBill", "出库取消接口"),
    GET_INVENTORY("getInventory", "库存获取")
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

    ThirdWarehouseBusinessTypeEnum(String code, String name) {
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
        for (ThirdWarehouseBusinessTypeEnum typeEnums : ThirdWarehouseBusinessTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
