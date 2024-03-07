package com.common.business.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 平台类型
 *
 * @Author Cloud
 * @Date 2023/8/28 10:47
 **/
@Getter
@AllArgsConstructor
public enum BusinessTypeEnum implements EnumMessage {
    // 销售订单
    ORDER("order","销售订单", SourceTypeEnum.SO_B2C),
    // TODO 补充来源类型
    REFUND("refund","退款单", null),
    PRODUCT("product","商品", SourceTypeEnum.LISTING_INFO),
    GLOBAL_PRODUCT("global_product","全球商品", SourceTypeEnum.LISTING_INFO),
    // TODO 补充来源类型
    RETURN("return","退货单", null),
    // TODO 补充来源类型
    DELIVERY("delivery","发货单", null),
    AUTH("auth","授权", null),
    REFRESH_TOKEN("refresh_token","刷新token",null),
    FBA_SHIPMENT("fba_shipment","亚马逊FBA货件", SourceTypeEnum.FBA_SHIPMENT),
    FBA_SHIPMENT_DETAIL("fba_shipment_detail","亚马逊FBA货件明细", SourceTypeEnum.FBA_SHIPMENT_DETAIL),
    FBA_INVENTORY("fba_inventory","亚马逊FBA仓库", SourceTypeEnum.FBA_INVENTORY),
    SO_OUT_STOCK("so_out_stock","销售出库单", null),

    WAREHOUSE("warehouse","仓库", SourceTypeEnum.THIRD_WAREHOUSE_GET_WAREHOUSE),
    CITY_DICT("city_dict","区域数据", SourceTypeEnum.THIRD_WAREHOUSE_GET_BASE_ADDRESS),
    GET_TRACK("getTrack","物流轨迹", SourceTypeEnum.LOGISTICS_SUPPLIER),
    TRANSFER("transfer","中转仓数据", SourceTypeEnum.THIRD_WAREHOUSE_GET_TRANSIT_WAREHOUSE_AND_LOGISTIC),
    INBOUND("inbound","获取收货批次", SourceTypeEnum.THIRD_WAREHOUSE_GET_INBOUND_RECEIPT),
    OUTBOUND("outbound","获取出库单状态", SourceTypeEnum.THIRD_WAREHOUSE_GET_OUTBOUND_RECEIPT),
    INVENTORY("inventory","库存", SourceTypeEnum.THIRD_WAREHOUSE_GET_INVENTORY)
    ;

    @JsonValue
    @EnumValue
    private final String code;

    private final String name;

    private final SourceTypeEnum sourceType;



    public static BusinessTypeEnum getByCode(String code) {
        for (BusinessTypeEnum state : BusinessTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static BusinessTypeEnum getByCodeAndThrow(String code) {
        BusinessTypeEnum businessType = getByCode(code);
        if (null == businessType){
            throw new ServiceException(StrUtil.format("业务类型business = {} 不存在", code));
        }
        SourceTypeEnum sourceType = businessType.getSourceType();
        if (ObjectUtil.isEmpty(sourceType)){
            throw new ServiceException(StrUtil.format("来源类型business = {} 不存在", code));
        }
        return businessType;
    }
}
