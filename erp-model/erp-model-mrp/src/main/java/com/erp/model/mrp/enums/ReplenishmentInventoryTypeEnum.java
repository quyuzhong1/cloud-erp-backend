package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ReplenishmentInventoryTypeEnum implements EnumMessage {

    FBA_IN_TRANSIT("FBA_IN_TRANSIT","FBA在途"),
    FBA_ESTIMATED_DELIVERY("FBA_ESTIMATED_DELIVERY","FBA预计发货"),
    OVERSEAS_USABLE("OVERSEAS_USABLE", "海外仓可用"),
    OVERSEAS_IN_TRANSIT("OVERSEAS_IN_TRANSIT", "海外仓在途"),
    OVERSEAS_ESTIMATED_DELIVERY("OVERSEAS_ESTIMATED_DELIVERY", "海外仓预计发货"),
    LOCAL_USABLE("LOCAL_USABLE", "本地可用"),
    LOCAL_IN_TRANSIT("LOCAL_IN_TRANSIT", "本地在途"),
    LOCAL_ESTIMATED_DELIVERY("LOCAL_ESTIMATED_DELIVERY", "预计采购")
    ;

    private final String code;

    private final String name;
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static ReplenishmentInventoryTypeEnum of(String code) {
        return Arrays.stream(ReplenishmentInventoryTypeEnum.values())
                .filter(v -> v.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_9028));
    }
}
