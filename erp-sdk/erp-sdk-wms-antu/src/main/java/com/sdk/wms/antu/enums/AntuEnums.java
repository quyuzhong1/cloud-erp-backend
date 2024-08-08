package com.sdk.wms.antu.enums;

import com.common.business.enums.OverseasInstockStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import io.seata.common.util.StringUtils;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AntuEnums {
    RECEIVING_TYPE("receivingType",TransitTypeEnum.class),
    PRODUCT_STATUS("productStatus",ProductStatusEnum.class),
    INCOME_TYPE("incomeType",IncomeTypeEnum.class),
    RECEIVING_STATUS("receivingStatus",ReceivingStatusEnum.class),
    CANCEL_STATUS("cancelStatus",CancelStatusEnum.class),
    ORDER_STATUS("orderStatus",OrderStatusEnum.class),
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    AntuEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }

    /**
     * 入库类型 D:自发头程,T中转代发
     */
    @Getter
    public enum TransitTypeEnum {
        SPONTANEOUS("D","自发头程", OverseasInstockTypeEnum.SELF_HEADWAY),
        TRANSFER("T","中转代发",OverseasInstockTypeEnum.TRANSFER_AGENT)
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
     * 产品状态枚举
     */
    @Getter
    public enum ProductStatusEnum {
        ABANDONMENT("X","废弃"),
        DRAFT("D","草稿"),
        AVAILABLE("S","可用"),
        UNDER_REVIEW("P","审核中"),
        REVIEW_FAILED("R","审核不通过"),
        ;
        private final String code;
        private final String name;
        ProductStatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 交货方式
     */
    @Getter
    public enum IncomeTypeEnum {
        SELF_DELIVERY(0,"自送", OverseasDeliveryModeEnum.SELF_DELIVERY),
        COLLECT(1,"揽收", OverseasDeliveryModeEnum.COLLECT_AT_HOME)
        ;
        private final Integer code;
        private final String name;
        private final OverseasDeliveryModeEnum erpEnum;

        IncomeTypeEnum(Integer code, String name, OverseasDeliveryModeEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static Integer getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(IncomeTypeEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(IncomeTypeEnum::getCode)
                    .orElse(null);
        }
    }

    /**
     * 入库单状态
     */
    @Getter
    public enum ReceivingStatusEnum {
        NEW("C","新建", OverseasInstockStatusEnum.TO_BE_SHIPPED),
        FIRST_JOURNEY_ON_THE_WAY("W","头程在途", OverseasInstockStatusEnum.TO_BE_SIGNED),
        INITIAL_RECEIVING("P","头程收货中", OverseasInstockStatusEnum.TO_BE_SIGNED),
        IN_TRANSIT("Z","转运中", OverseasInstockStatusEnum.TO_BE_SIGNED),
        RECEIVING_DESTINATION_WAREHOUSE("G","目的仓库收货中", OverseasInstockStatusEnum.PARTIAL_SIGNED),
        COMPLETION_RECEIVING_DESTINATION_WAREHOUSE("F","目的仓收货完成", OverseasInstockStatusEnum.PARTIAL_SIGNED),
        COMPLETE_LISTING("E","完成上架", OverseasInstockStatusEnum.SIGNED),
        ABANDONMENT("X","废弃", OverseasInstockStatusEnum.CANCELED)
        ;
        private final String code;
        private final String name;
        private final OverseasInstockStatusEnum instockStatusEnum;
        ReceivingStatusEnum(String code, String name, OverseasInstockStatusEnum instockStatusEnum) {
            this.code = code;
            this.name = name;
            this.instockStatusEnum = instockStatusEnum;
        }

        public static String getInstockByCode(String code){
            return Arrays.stream(ReceivingStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(ReceivingStatusEnum::getInstockStatusEnum)
                    .map(OverseasInstockStatusEnum::getCode)
                    .orElse(code);
        }

    }
    /**
     * 取消订单结果
     */
    @Getter
    public enum CancelStatusEnum {
        INTERCEPTING(1,"拦截中"),
        INTERCEPTION_SUCCESSFUL(2,"拦截成功"),
        INTERCEPTION_FAILED(3,"拦截失败"),
        SUCCESSFULLY_INTERCEPTED_BUT_NOT_REFUNDED_FEES(4,"拦截成功但未退费用"),
        ;
        private final Integer code;
        private final String name;
        CancelStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }


    /**
     * 入库单状态
     */
    @Getter
    public enum OrderStatusEnum {
        NEW("C","待发货审核",SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        FIRST_JOURNEY_ON_THE_WAY("W","待发货", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        INITIAL_RECEIVING("D","已发货", SoB2cBillStatusEnum.ENUM_SHIPPED),
        IN_TRANSIT("H","暂存", null),
        RECEIVING_DESTINATION_WAREHOUSE("N","异常订单", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        COMPLETION_RECEIVING_DESTINATION_WAREHOUSE("P","问题件", null),
        ABANDONMENT("X","废弃", null)
        ;
        private final String code;
        private final String name;
        private final SoB2cBillStatusEnum erpSoStatus;


        OrderStatusEnum(String code, String name, SoB2cBillStatusEnum erpsoStatus) {
            this.code = code;
            this.name = name;
            this.erpSoStatus = erpsoStatus;
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
            return Arrays.stream(AntuEnums.OrderStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(AntuEnums.OrderStatusEnum::getName)
                    .orElse(null);
        }
    }
}
