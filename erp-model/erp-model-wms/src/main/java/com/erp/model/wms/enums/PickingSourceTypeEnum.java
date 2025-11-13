package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 装箱任务-单据类型
 */
public enum PickingSourceTypeEnum implements EnumMessage {
    FBA("FBA", "FBA"),
    THIRD("thirdWarehouse", "第三方仓"),
    ALIEXPRESS("aliexpress", "速卖通仓"),
    B2B("B2B", "B2B");
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    PickingSourceTypeEnum(String code, String name) {
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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (PickingSourceTypeEnum item : PickingSourceTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static PickingSourceTypeEnum getByStatus(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }

    public static List<String> getCodeList() {
        return Arrays.stream(PickingSourceTypeEnum.values()).map(PickingSourceTypeEnum::getCode).collect(Collectors.toList());
    }
}
