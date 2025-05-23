package com.sdk.wms.jifeng.enums;


import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum JiFengEnums {
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    JiFengEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }
    /**
     * 出库单状态
     */
    @Getter
    public enum OrderStatusEnum {
        PENDING_MOVES("1","待移货", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_GENERATED("2","待生成", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_PICKED("3","待拣货", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_PACKED("4","待包装", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        IN_THE_PACKAGE("5","包装中", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_SHIPPED("6","待发货", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        SHIPPED("7","已发货", SoB2cBillStatusEnum.ENUM_SHIPPED),
        ABNORMAL("8","异常", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        CANCELED("9","已取消", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        GETTING_IT("10","获取中",SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        FAILED_TO_GET("11","获取失败",SoB2cBillStatusEnum.ENUM_EXCEPTION),
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
     * 出库单拦截状态
     */
    @Getter
    public enum InterceptStatusEnum {
        PENDING_MOVES("1","截单中", ThirdWarehouseCancelResultEnum.INTERCEPTING),
        TO_BE_GENERATED("2","截单成功", ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL),
        TO_BE_PICKED("3","截单失败", ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED),
        ;
        private final String code;
        private final String name;
        private final ThirdWarehouseCancelResultEnum erpSoStatus;

        InterceptStatusEnum(String code, String name,ThirdWarehouseCancelResultEnum erpSoStatus) {
            this.code = code;
            this.name = name;
            this.erpSoStatus = erpSoStatus;
        }

        public static String getErpOrderStatus(String code){
            return Arrays.stream(InterceptStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(InterceptStatusEnum::getErpSoStatus)
                    .map(ThirdWarehouseCancelResultEnum::getCode)
                    .orElse("");
        }

        public static String getName(String code){
            return Arrays.stream(InterceptStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(InterceptStatusEnum::getName)
                    .orElse(null);
        }
    }
}
