package com.sdk.wms.weishi.enums;


import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.*;
import io.seata.common.util.StringUtils;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum WeiShiEnums {
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    WeiShiEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }


    /**
     * 出库单状态
     */
    @Getter
    public enum OrderStatusEnum {
        PENDING_MOVES("0","草稿", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_GENERATED("1","提交", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_PICKED("2","待拣货", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_PACKED("3","拣货中", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        IN_THE_PACKAGE("4","拣货完成", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        TO_BE_SHIPPED("5","打包完成", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        SHIPPED("6","出库完成", SoB2cBillStatusEnum.ENUM_SHIPPED),
        CANCELED("9","已取消", SoB2cBillStatusEnum.ENUM_DISUSE),
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
     * 运输方式
     */
    @Getter
    public enum ProductCodeEnum {
        AIR_TRANSPORT("AIRLIFT","空运", LogisticsMethodEnum.AIRFREIGHT),
        EXPRESS("EXPRESS","快递", LogisticsMethodEnum.EXPRESS),
        SEA_FREIGHT_BULK_CARGO("SEA_FREIGHT","海运散货", LogisticsMethodEnum.OCEAN_FREIGHT_BULK),
        RAIL_TRANSPORT_FULL_CONTAINER("SEA_FREIGHT","铁运整柜", LogisticsMethodEnum.RAILWAY_TRANSPORTATION_FCL),
        OCEAN_FREIGHT_FULL_CONTAINER("SEA_FREIGHT","海运整柜", LogisticsMethodEnum.OCEAN_FREIGHT_FCL),
        RAIL_FREIGHT_BULK_CARGO("SEA_FREIGHT","铁运散货", LogisticsMethodEnum.RAILWAY_TRANSPORTATION_BULK),
        LOCAL_DELIVERY("LOCAL_DELIVERY","本地发运", null),
        ;
        private final String code;
        private final String name;
        private final LogisticsMethodEnum erpEnum;

        ProductCodeEnum(String code, String name, LogisticsMethodEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static String getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(ProductCodeEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(ProductCodeEnum::getCode)
                    .orElse(null);
        }
    }

    /**
     * 入库类型 D:自发头程,T中转代发
     */
    @Getter
    public enum TransitTypeEnum {
        SPONTANEOUS("CUSTOMER","自发头程", OverseasInstockTypeEnum.SELF_HEADWAY),
        TRANSFER("WAREHOUSE","中转代发",OverseasInstockTypeEnum.TRANSFER_AGENT)
        ;
        private final String code;
        private final String name;
        private final OverseasInstockTypeEnum erpEnum;

        TransitTypeEnum(String code, String name, OverseasInstockTypeEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static String getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(TransitTypeEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(TransitTypeEnum::getCode)
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


    /**
     * 退件类型
     */
    @Getter
    public enum ReturnInstockTypeEnum {
        RETURNS_FROM_SERVICE_PROVIDERS(1,"服务商退件", ReturnTypeEnum.RETURNS_FROM_SERVICE_PROVIDERS),
        CUSTOMER_RETURNS(2,"客户退件", ReturnTypeEnum.CUSTOMER_RETURNS),
        ;
        private final Integer code;
        private final String name;
        private final ReturnTypeEnum erpEnum;

        ReturnInstockTypeEnum(Integer code, String name, ReturnTypeEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static Integer getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(ReturnInstockTypeEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(ReturnInstockTypeEnum::getCode)
                    .orElse(null);
        }

        public static ReturnInstockTypeEnum getByCode(Integer code) {
            return Arrays.stream(ReturnInstockTypeEnum.values())
                    .filter(e -> e.getCode().equals(code))
                    .findFirst()
                    .orElse(null);
        }

        public static String getErpStatus(String code){
            return Arrays.stream(ReturnInstockTypeEnum.values())
                    .filter(item -> item.getCode().toString().equalsIgnoreCase(code))
                    .findFirst()
                    .map(ReturnInstockTypeEnum::getErpEnum)
                    .map(ReturnTypeEnum::getCode)
                    .orElse("");
        }
    }
}
