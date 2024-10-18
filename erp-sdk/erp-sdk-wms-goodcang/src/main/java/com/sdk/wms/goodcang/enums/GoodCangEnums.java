package com.sdk.wms.goodcang.enums;


import com.common.business.enums.OverseasInstockStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cReturnTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.OverseasCustomsTypeNewEnum;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import io.seata.common.util.StringUtils;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum GoodCangEnums {
    CONTAIN_BATTERY("containBattery",GoodsAttributeEnum.class),
    PRODUCT_STATUS("productStatus",OpenApiProductStatusEnum.class),
    TYPE_OF_GOODS("typeOfGoods",ProductPackageTypeEnum.class),
    CAT_LANG("catLang",LanguageEnum.class),
    HEAD_TYPE("headType",HeadTypeEnum.class),
    SKU_WRAPPER_TYPE("skuWrapperType",ProductSkuWrapperTypeEnum.class),
    RECEIVING_STATUS("receivingStatus",OpenReceivingStatusEnum.class),
    TRANSIT_TYPE("transitType",OpenTransitTypeEnum.class),
    RECEIVING_SHIPPING_TYPE("receivingShippingType",ProductCodeEnum.class),
    CUSTOMS_TYPE("customsType",CustomsTypeNewEnum.class),
    COLLECTING_SERVICE("collectingService",OpenCollectingServiceEnum.class),
    BUSINESS_TYPE("businessType",OrderBusinessTypeEnum.class),
    LOGISTICS_RECOMMENDATION_OPTION("logisticsRecommendationOption",NewLogisticsTimelinessEnum.class),
    LABEL_REPLACEMENT_OPTION("labelReplacementOption",FbaOrderLabelReplacementOptionEnum.class),
    CUSTOMER_PACKAGE_REQUIREMENT("customerPackageRequirement",PackageReqEnum.class),
    PRODUCT_FREEZE_STATUS("productFreezeStatus",InventoryProductFreezeStatusEnum.class),
    CANCEL_STATUS("cancelStatus",OrderCancelStatusEnum.class),
    ORDER_STATUS("orderStatus",OrderCancelStatusEnum.class),
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    GoodCangEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }

    /**
     * 货物属性枚举
     */
    @Getter
    public enum GoodsAttributeEnum {
        GENERAL_CARGO(0,"普货"),
        INCLUDING_BATTERY(1,"含电池"),
        PURE_BATTERY(2,"纯电池"),
        TEXTILE(3,"纺织品"),
        FRAGILE_PRODUCTS(4,"易碎品"),
        EXCEEDING_STANDARD_PURE_BATTERIES(6,"超标纯电池"),
        EXCEEDING_STANDARD_WITH_BATTERIES(7,"超标含电池")
        ;
        private final Integer code;
        private final String name;
        GoodsAttributeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 包裹类型枚举
     */
    @Getter
    public enum ProductPackageTypeEnum {
        PACKAGE(0,"包裹"),
        ENVELOPE(1,"信封")
        ;
        private final Integer code;
        private final String name;

        ProductPackageTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    /**
     * 包裹类型枚举
     */
    @Getter
    public enum OpenApiProductStatusEnum {
        ABANDONMENT("X","废弃"),
        AVAILABLE("S","可用"),
        DRAFT("D","草稿"),
        UNDER_REVIEW("W","审核中/待审核"),
        REJECT("R","驳回")
        ;
        private final String code;
        private final String name;

        OpenApiProductStatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 包裹类型枚举
     */
    @Getter
    public enum LanguageEnum {
        ZH("zh","中文"),
        EN("en","英文")
        ;
        private final String code;
        private final String name;

        LanguageEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 头程类型
     */
    @Getter
    public enum HeadTypeEnum {
        ZH("GC","谷仓头程"),
        EN("MJ","卖家直发")
        ;
        private final String code;
        private final String name;

        HeadTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * SKU包装属性
     */
    @Getter
    public enum ProductSkuWrapperTypeEnum {
        PRE_PACKAGING(1,"预包装"),
        SALES_PACKAGING(2,"销售包装"),
        ORIGINAL_PACKAGING_COLOR_BOX(3,"原包彩盒")
        ;
        private final Integer code;
        private final String name;

        ProductSkuWrapperTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 入库单状态
     */
    @Getter
    public enum OpenReceivingStatusEnum {
        DRAFT(0,"草稿", OverseasInstockStatusEnum.TO_BE_SHIPPED),
        PENDING_REVIEW(1,"待审核", OverseasInstockStatusEnum.TO_BE_SHIPPED),
        REVIEW_FAILED(2,"审核不通过", OverseasInstockStatusEnum.ABNORMAL),
        TRANSFER_WAREHOUSE_TO_BE_SIGNED(3,"中转仓待签收", OverseasInstockStatusEnum.TO_BE_SIGNED),
        TRANSFER_WAREHOUSE_TO_BE_RECEIVED(4,"中转仓待收货", OverseasInstockStatusEnum.TO_BE_SIGNED),
        TRANSIT_WAREHOUSE_WAITING_DISTRIBUTION(5,"中转仓待配货", OverseasInstockStatusEnum.TO_BE_SIGNED),
        TRANSIT_WAREHOUSE_TO_BE_SHIPPED(6,"中转仓待发货", OverseasInstockStatusEnum.TO_BE_SIGNED),
        OVERSEAS_WAREHOUSE_IN_TRANSIT(7,"海外仓在途", OverseasInstockStatusEnum.TO_BE_SIGNED),
        RECEIVING_FROM_OVERSEAS_WAREHOUSES(8,"海外仓收货中", OverseasInstockStatusEnum.PARTIAL_SIGNED),
        COMPLETION_OVERSEAS_WAREHOUSE_RECEIPT(9,"海外仓收货完成", OverseasInstockStatusEnum.PARTIAL_SIGNED),
        COMPLETION_OF_OVERSEAS_WAREHOUSE_LISTING(10,"海外仓上架完成", OverseasInstockStatusEnum.SIGNED),
        ABANDONMENT(100,"废弃", OverseasInstockStatusEnum.CANCELED),
        ;
        private final Integer code;
        private final String name;
        private final OverseasInstockStatusEnum instockStatusEnum;

        OpenReceivingStatusEnum(Integer code, String name,OverseasInstockStatusEnum instockStatusEnum) {
            this.code = code;
            this.name = name;
            this.instockStatusEnum = instockStatusEnum;
        }

        public static String getInstockByCode(Integer code){
            return Arrays.stream(OpenReceivingStatusEnum.values())
                    .filter(item -> code.equals(item.getCode()))
                    .findFirst()
                    .map(OpenReceivingStatusEnum::getInstockStatusEnum)
                    .map(OverseasInstockStatusEnum::getCode)
                    .orElse(code.toString());
        }
    }

    /**
     * 入库单类型
     */
    @Getter
    public enum OpenTransitTypeEnum {
        STANDARD_RECEIPT(0,"标准入库单",OverseasInstockTypeEnum.SELF_HEADWAY),
        TRANSFER_RECEIPT(3,"中转入库单(标准货运单)", OverseasInstockTypeEnum.TRANSFER_AGENT),
        FBA_WAREHOUSE_RECEIPT(5,"FBA入库单",null),
        ;
        private final Integer code;
        private final String name;
        private final OverseasInstockTypeEnum erpEnum;

        OpenTransitTypeEnum(Integer code, String name, OverseasInstockTypeEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static Integer getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(OpenTransitTypeEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(OpenTransitTypeEnum::getCode)
                    .orElse(null);
        }
    }

    /**
     * 运输方式
     */
    @Getter
    public enum ProductCodeEnum {
        AIR_TRANSPORT(0,"空运", LogisticsMethodEnum.AIRFREIGHT),
        SEA_FREIGHT_BULK_CARGO(1,"海运散货", LogisticsMethodEnum.OCEAN_FREIGHT_BULK),
        EXPRESS(2,"快递", LogisticsMethodEnum.EXPRESS),
        RAIL_TRANSPORT_FULL_CONTAINER(3,"铁运整柜", LogisticsMethodEnum.RAILWAY_TRANSPORTATION_FCL),
        OCEAN_FREIGHT_FULL_CONTAINER(4,"海运整柜", LogisticsMethodEnum.OCEAN_FREIGHT_FCL),
        RAIL_FREIGHT_BULK_CARGO(5,"铁运散货", LogisticsMethodEnum.RAILWAY_TRANSPORTATION_BULK)
        ;
        private final Integer code;
        private final String name;
        private final LogisticsMethodEnum erpEnum;

        ProductCodeEnum(Integer code, String name, LogisticsMethodEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static Integer getCodeByErp(String erpCode){
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
     * 报关方式
     */
    @Getter
    public enum CustomsTypeNewEnum {
        AGENCY_CUSTOMS_DECLARATION(0,"贸易代理报关", OverseasCustomsTypeNewEnum.AGENCY_CUSTOMS_DECLARATION),
        REFUND_CUSTOMS_DECLARATION(1,"退税报关", OverseasCustomsTypeNewEnum.REFUND_CUSTOMS_DECLARATION),
        SELF_CUSTOMS_DECLARATION(2,"报关自理", OverseasCustomsTypeNewEnum.SELF_CUSTOMS_DECLARATION)
        ;
        private final Integer code;
        private final String name;
        private final OverseasCustomsTypeNewEnum erpEnum;

        CustomsTypeNewEnum(Integer code, String name, OverseasCustomsTypeNewEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static Integer getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(CustomsTypeNewEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(CustomsTypeNewEnum::getCode)
                    .orElse(null);
        }

        public static CustomsTypeNewEnum getByCode(Integer code) {
            return Arrays.stream(CustomsTypeNewEnum.values())
                    .filter(e -> e.getCode().equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 揽收服务
     */
    @Getter
    public enum OpenCollectingServiceEnum {
        SELF_DELIVERED_GOODS(0,"自送货物",OverseasDeliveryModeEnum.SELF_DELIVERY),
        PICK_UP(1,"上门提货", OverseasDeliveryModeEnum.COLLECT_AT_HOME)
        ;
        private final Integer code;
        private final String name;
        private final OverseasDeliveryModeEnum erpEnum;

        OpenCollectingServiceEnum(Integer code, String name, OverseasDeliveryModeEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }

        public static Integer getCodeByErp(String erpCode){
            if(StringUtils.isBlank(erpCode)){
                return null;
            }
            return Arrays.stream(OpenCollectingServiceEnum.values())
                    .filter(item -> erpCode.equals(item.getErpEnum().getCode()))
                    .findFirst()
                    .map(OpenCollectingServiceEnum::getCode)
                    .orElse(null);
        }
    }

    /**
     * 配送方式
     */
    @Getter
    public enum OrderBusinessTypeEnum {
        SELF_DELIVERED_GOODS(0,"仓配一体"),
        PICK_UP(1,"仓配分离")
        ;
        private final Integer code;
        private final String name;

        OrderBusinessTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    /**
     * 物流优选时效枚举选项
     */
    @Getter
    public enum NewLogisticsTimelinessEnum {
        ONE_TO_THREE_DAY(1,"预估1-3个工作日"),
        THREE_TO_SIX_DAY(2,"预估3-6个工作日"),
        LEAST_SIX_DAY(3,"预估6个工作日以上"),
        FASTEST_ESTIMATION_TIME(4,"预估时效最快"),
        ESTIMATED_COST_SAVINGS(5,"预估费用最省")
        ;
        private final Integer code;
        private final String name;

        NewLogisticsTimelinessEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 换标要求
     */
    @Getter
    public enum FbaOrderLabelReplacementOptionEnum {
        ONE_TO_THREE_DAY(1,"外箱"),
        THREE_TO_SIX_DAY(2,"内箱")
        ;
        private final Integer code;
        private final String name;

        FbaOrderLabelReplacementOptionEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 订单包装要求
     */
    @Getter
    public enum PackageReqEnum {
        CARTON(1,"纸箱"),
        EXPRESS_BAG(2,"快递袋"),
        BUBBLE_BAG(3,"气泡袋"),
        ENVIRONMENTALLY_FRIENDLY_BAGS(4,"环保袋")
        ;
        private final Integer code;
        private final String name;

        PackageReqEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 商品冻结状态
     */
    @Getter
    public enum InventoryProductFreezeStatusEnum {
        NORMAL(0,"正常"),
        FREEZE(1,"冻结"),
        THAWING(2,"解冻")
        ;
        private final Integer code;
        private final String name;

        InventoryProductFreezeStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 商品冻结状态
     */
    @Getter
    public enum OrderCancelStatusEnum {
        SUCCESSFUL(2,"拦截成功"),
        FAILED(3,"拦截失败")
        ;
        private final Integer code;
        private final String name;

        OrderCancelStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }


    /**
     * 出库单状态
     */
    @Getter
    public enum OrderStatusEnum {
        TO_BE_SHIPPED("W","待发货",SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        SHIPPED("D","已发货",SoB2cBillStatusEnum.ENUM_SHIPPED),
        ABNORMAL("N","异常订单",SoB2cBillStatusEnum.ENUM_EXCEPTION),
        PROBLEM("P","问题件", null),
        REMOVED("X","已删除",null),
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
     * 退件类型
     */
    @Getter
    public enum ReturnInstockTypeEnum {
        RETURNS_FROM_SERVICE_PROVIDERS(0,"服务商退件", SoB2cReturnTypeEnum.RETURNS_FROM_SERVICE_PROVIDERS),
        CUSTOMER_RETURNS(1,"客户退件", SoB2cReturnTypeEnum.CUSTOMER_RETURNS),
        ;
        private final Integer code;
        private final String name;
        private final SoB2cReturnTypeEnum erpEnum;

        ReturnInstockTypeEnum(Integer code, String name, SoB2cReturnTypeEnum erpEnum) {
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
                    .map(SoB2cReturnTypeEnum::getCode)
                    .orElse("");
        }
    }
}
