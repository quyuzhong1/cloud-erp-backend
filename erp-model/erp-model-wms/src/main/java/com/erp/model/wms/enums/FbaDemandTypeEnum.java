package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum FbaDemandTypeEnum implements EnumMessage {
    DEMAND_PLATFORM_WAREHOUSE("demandPlatformWarehouse", "备货FBA仓"),
    DEMAND_OVERSEAS_WAREHOUSE("demandOverseasWarehouse", "备货第三方仓"),
    DEMAND_ALIEXPRESS("demandAliexpress", "备货速卖通仓"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    FbaDemandTypeEnum(String code, String name) {
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
            for (FbaDemandTypeEnum item : FbaDemandTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
