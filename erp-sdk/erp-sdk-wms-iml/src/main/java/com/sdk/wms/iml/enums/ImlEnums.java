package com.sdk.wms.iml.enums;

import com.common.business.enums.OverseasInstockStatusEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ImlEnums {
    RECEIVING_TYPE("receivingType",TransitTypeEnum.class),
    PRODUCT_STATUS("productStatus",ProductStatusEnum.class),
    INCOME_TYPE("incomeType",IncomeTypeEnum.class),
    RECEIVING_STATUS("receivingStatus",ReceivingStatusEnum.class),
    CANCEL_STATUS("cancelStatus",CancelStatusEnum.class),
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    ImlEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }

    /**
     * 入库类型 D:自发头程,T中转代发
     */
    @Getter
    public enum TransitTypeEnum {
        DRAFT("D","自发头程"),
        AVAILABLE("T","T中转代发")
        ;
        private final String code;
        private final String name;
        TransitTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
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
        SELF_DELIVERY(0,"自送"),
        COLLECT(1,"揽收")
        ;
        private final Integer code;
        private final String name;
        IncomeTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
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
        COMPLETION_RECEIVING_DESTINATION_WAREHOUSE("F","目的仓收货完成", OverseasInstockStatusEnum.SIGNED),
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
}
