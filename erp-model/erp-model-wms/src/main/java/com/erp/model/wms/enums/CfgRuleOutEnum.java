package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库配置枚举
 */
@Getter
public enum CfgRuleOutEnum {
    ;

    @Getter
    public enum CfgRuleOutTypeEnum implements EnumMessage  {
        EQUIPMENT_SORTING_PORT("equipmentSortingPort", "设备分拣口"),
        STOCK_OUT_TRANSFER("stockOutTransfer", "出库中转配置"),
        B2C_ALLOWABLE_DEVIATIONS("b2cAllowableDeviations", "B2C称重量方允许偏差"),
        CFG_PACKING_OVER_WEIGHT("cfgPackingOverWeight", "装箱超重配置"),
        CFG_PRODUCT_PACKING("cfgProductPacking", "产品装箱配置"),
        ;
        private String code;
        private String name;
        CfgRuleOutTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum EquipmentSortingPortTypeEnum implements EnumMessage  {
        LOGISTIC_SUPPLIER("logisticSupplier", "按物流商设置"),
        CHANNEL("channel", "按渠道设置");
        private String code;
        private String name;
        EquipmentSortingPortTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum EquipmentSortingPortCompareEnum implements EnumMessage  {
        EQ("eq", "等于"),
        NQ("nq", "不等于"),
        IN_LIST("inList", "在列表"),
        NOT_IN_LIST("notInList", "不在列表");
        private String code;
        private String name;
        EquipmentSortingPortCompareEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum EquipmentSortingPortEnum implements EnumMessage  {
        ONE("1", "1"),
        TWO("2", "2"),
        THREE("3", "3"),
        FOUR("4", "4"),
        FIVE("5", "5"),
        SIX("6", "6"),
        SEVEN("7", "7"),
        EIGHT("8", "8"),
        NINE("9", "9(异常口)");
        private String code;
        private String name;
        EquipmentSortingPortEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum AllowableDeviationsTypeEnum implements EnumMessage  {
        LOGISTIC_SUPPLIER("logisticSupplier", "按物流商设置"),
        CHANNEL("channel", "按渠道设置");
        private String code;
        private String name;
        AllowableDeviationsTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum AllowableDeviationsConditionEnum implements EnumMessage  {
        WEIGHING_VARIANCE_RATE("weightVarianceRate", "称重差异率(%)"),
        VOLUME_DIFFERENCE_RATE_LONG("volumeDifferenceRateLength", "量方差异率-长(%)"),
        VOLUME_DIFFERENCE_RATE_WIDTH("volumeDifferenceRateWidth", "量方差异率-宽(%)"),
        VOLUME_DIFFERENCE_RATE_HEIGHT("volumeDifferenceRateHeight", "量方差异率-高(%)"),
        WEIGHING_VARIANCE_VALUE("weightVarianceValue", "称重差异值(g)"),
        VOLUME_DIFFERENCE_VALUE_LONG("volumeDifferenceValueLength", "量方差异值-长(cm)"),
        VOLUME_DIFFERENCE_VALUE_WIDTH("volumeDifferenceValueWidth", "量方差异值-宽(cm)"),
        VOLUME_DIFFERENCE_VALUE_HEIGHT("volumeDifferenceValueHeight", "量方差异值-高(cm)");
        private String code;
        private String name;
        AllowableDeviationsConditionEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    @Getter
    public enum AllowableDeviationsCompareEnum implements EnumMessage  {
        GREATER_THAN(">", "大于"),
        GREATER_THAN_OR_EQUAL_TO(">=", "大于等于"),
        LESS_THAN("<", "小于"),
        LESS_THAN_OR_EQUAL_TO("<=", "小于等于");
        private String code;
        private String name;
        AllowableDeviationsCompareEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum AllowableDeviationsLogicEnum implements EnumMessage  {
        AND("and", "且"),
        OR("or", "或");
        private String code;
        private String name;
        AllowableDeviationsLogicEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum StockOutTransferCompareEnum implements EnumMessage {
        EQ("==", "等于"),
        NQ("!=", "不等于"),
        IN_LIST("inList", "在列表"),
        NOT_IN_LIST("notInList", "不在列表"),
        ;

        private String code;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public enum StockOutTransferFieldEnum implements EnumMessage{
        RECEIVE_COUNTRY("receiveCountry", "收货国家"),
        DEST_WAREHOUSE("destWarehouse", "目的仓库"),
        ;
        private String code;
        private String name;
    }
}
