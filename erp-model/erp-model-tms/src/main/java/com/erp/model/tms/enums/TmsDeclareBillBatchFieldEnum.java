package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 报关单批量更新字段枚举（主表）
 */
public enum TmsDeclareBillBatchFieldEnum implements EnumMessage {
    PRE_INPUT_NO("pre_input_no", "预录入编号"),
    DEST_CUSTOMS("dest_customs", "申报地海关"),
    DECLARE_TYPE("declare_type", "报关类型"),
    SENDER_ID("sender_id", "境内发货人"),
    RECEIVER_ID("receiver_id", "境外收货人"),
    EXPORT_CUSTOMS_NAME("export_customs_name", "出境关别"),
    EXPORT_DATE("export_date", "出口日期"),
    DECLARE_DATE("declare_date", "申报日期"),
    DICT_SUPERVISION_METHOD("dict_supervision_method", "监管方式"),
    DICT_NATURE_LEVY("dict_nature_levy", "征免性质"),
    LICENSE_NO("license_no", "许可证号"),
    TRADING_AREA("trading_area", "贸易国地区"),
    TO_AREA("to_area", "抵运国地区"),
    TO_PORT("to_port", "抵运港地区"),
    EXPORT_PORT("export_port", "出境口岸"),
    DICT_PACK_TYPE("dict_pack_type", "包装种类"),
    DICT_TRANSACTION_METHOD("dict_transaction_method", "成交方式"),
    REMARK("remark", "标记唛码及备注"),
    SHIPPING_FEE("shipping_fee", "运费"),
    INSURANCE_FEE("insurance_fee", "保费"),
    OTHER_FEE("other_fee", "杂费"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    TmsDeclareBillBatchFieldEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static TmsDeclareBillBatchFieldEnum getEnumByCode(String code) {
        for (TmsDeclareBillBatchFieldEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
