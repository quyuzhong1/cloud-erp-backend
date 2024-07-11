package com.sdk.wangdian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 旺店通其他出库单状态枚举
 * @date 2024-07-08
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum WdtOutStockStatusEnum {
    CANCEL("5", "已取消"),
    NOT_CONFIRM("48", "未确认"),
    WAIT_APPROVE("50", "待审核"),
    WAIT_HANDLE("65", "待处理"),
    PICK_ING("77", "拣货中"),
    FINISH("110", "已完成");

    private String no;
    private String name;

    public static String getName(String no){
        for (WdtOutStockStatusEnum statusEnum : WdtOutStockStatusEnum.values()) {
            if(statusEnum.getNo().equals(no)){
                return statusEnum.getName();
            }
        }
        return "";
    }
}
