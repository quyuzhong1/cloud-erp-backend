package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 寄样费用尾程费用导入费用项
 */
@Getter
@AllArgsConstructor
public enum KolSampleCostImportFeeTypeEnum implements EnumMessage {

    LOGISTICS_FEE("logisticsFee", "物流费", "尾程-运费"),
    ORDER_FEE("orderFee", "订单费用", "尾程-其他费用"),
    CUSTOMS_TAX("customsTax", "关税", "尾程-关税"),
    ;

    private final String code;

    private final String name;

    /**
     * 寄样成本页面展示字段
     */
    private final String costFieldName;

    public static KolSampleCostImportFeeTypeEnum getByName(String name) {
        String trimName = name == null ? null : name.trim();
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.getName(), trimName))
                .findFirst()
                .orElse(null);
    }

    public static String getSupportedNameTips() {
        return Arrays.stream(values())
                .map(item -> "【" + item.getName() + "】")
                .collect(Collectors.joining("或"));
    }
}
