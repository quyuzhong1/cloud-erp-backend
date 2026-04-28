package com.sdk.wms.damai.enums;


import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import io.seata.common.util.StringUtils;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DaMaiEnums {
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    DaMaiEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }
    /**
     * 出库单状态
     */
    @Getter
    public enum OrderStatusEnum {
        SUBMIT("SUBMIT","已提交", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        WAIT_PROCESSED("WAIT_PROCESSED","预报成功", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        PROCESSED("PROCESSED","出库中", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        SUCCESS("SUCCESS","已出库", SoB2cBillStatusEnum.ENUM_SHIPPED),
        DISCARD_PROCESSED("DISCARD_PROCESSED","取消中",null),
        DISCARD("DISCARD","已取消", SoB2cBillStatusEnum.ENUM_DISUSE),
        PROBLEM("PROBLEM","问题件", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        EXCEPTION ("EXCEPTION","出库异常", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        BLOCK ("BLOCK","拦截中",null),
        ;
        private final String code;
        private final String name;
        private final SoB2cBillStatusEnum erpSoStatus;

        OrderStatusEnum(String code, String name,SoB2cBillStatusEnum erpSoStatus) {
            this.code = code;
            this.name = name;
            this.erpSoStatus = erpSoStatus;
        }

        public static String getErpOrderStatus(String code){
            return Arrays.stream(OrderStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(OrderStatusEnum::getErpSoStatus)
                    .map(SoB2cBillStatusEnum::getCode)
                    .orElse("");
        }

        public static String getName(String code){
            return Arrays.stream(OrderStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(OrderStatusEnum::getName)
                    .orElse(null);
        }
    }

    /**
     * B2B三方发货单状态
     */
    @Getter
    public enum B2BOrderStatusEnum {
        SUBMIT("SUBMIT","已提交", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
        WAIT_PROCESSED("WAIT_PROCESSED","预报成功", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
        PROCESSED("PROCESSED","出库中", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
        SUCCESS("SUCCESS","已出库", ThirdDeliveryStatusEnum.SHIPPED),
        DISCARD_PROCESSED("DISCARD_PROCESSED","取消中", ThirdDeliveryStatusEnum.INTERCEPTING),
        DISCARD("DISCARD","已取消", ThirdDeliveryStatusEnum.CANCEL_DELIVERY),
        PROBLEM("PROBLEM","问题件", ThirdDeliveryStatusEnum.CANCEL_DELIVERY),
        EXCEPTION ("EXCEPTION","出库异常", ThirdDeliveryStatusEnum.EXCEPTION_ORDER),
        BLOCK ("BLOCK","拦截中", ThirdDeliveryStatusEnum.INTERCEPTING),
        ;
        private final String code;
        private final String name;
        private final ThirdDeliveryStatusEnum erpSoStatus;

        B2BOrderStatusEnum(String code, String name, ThirdDeliveryStatusEnum erpSoStatus) {
            this.code = code;
            this.name = name;
            this.erpSoStatus = erpSoStatus;
        }

        public static String getErpOrderStatus(String code){
            return Arrays.stream(B2BOrderStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(B2BOrderStatusEnum::getErpSoStatus)
                    .map(ThirdDeliveryStatusEnum::getCode)
                    .orElse("");
        }
    }
}
