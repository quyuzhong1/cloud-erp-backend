package com.sdk.wangdian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 旺店通其他入库单状态枚举
 * @date 2024-07-08
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum WdtExtOutStockStatusEnum {

    CANCEL("10", "已取消"),
    EDIT_ING("20", "编辑中"),
    WAIT_APPROVE("30", "待审核"),
    WAIT_CONFIRM("40", "待结算"),
    FINISH("50", "已完成");

    private final String status;
    private final String name;

    public static String getName(String status){
        return Arrays.stream(WdtExtOutStockStatusEnum.values())
                .filter(v -> v.getStatus().equals(status))
                .map(WdtExtOutStockStatusEnum::getName)
                .findFirst()
                .orElse("");
    }
}
