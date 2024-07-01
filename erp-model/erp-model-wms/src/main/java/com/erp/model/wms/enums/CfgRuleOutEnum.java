package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
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
        B2C_ALLOWABLE_DEVIATIONS("b2cAllowableDeviations", "B2C称重量方允许偏差");
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
        ONE("eq", "1"),
        TWO("nq", "2"),
        THREE("inList", "3"),
        FOUR("notInList", "4"),
        FIVE("notInList", "5"),
        SIX("notInList", "6"),
        SEVEN("notInList", "7"),
        EIGHT("notInList", "8"),
        NINE("notInList", "9(异常口)");
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
        WEIGHING_VARIANCE_RATE("weighingVarianceRate", "称重差异率(%)"),
        VOLUME_DIFFERENCE_RATE_LONG("volumeDifferenceRateLong", "量方差异率-长(%)"),
        VOLUME_DIFFERENCE_RATE_WIDTH("volumeDifferenceRateWidth", "量方差异率-宽(%)"),
        VOLUME_DIFFERENCE_RATE_HEIGHT("volumeDifferenceRateHeight", "量方差异率-高(%)"),
        WEIGHING_VARIANCE_VALUE("weighingVarianceValue", "称重差异值(g)"),
        VOLUME_DIFFERENCE_VALUE_LONG("volumeDifferenceValueLong", "量方差异值-长(cm)"),
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
        EQ("==", "等于"),
        NE("!=", "不等于"),
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
}
