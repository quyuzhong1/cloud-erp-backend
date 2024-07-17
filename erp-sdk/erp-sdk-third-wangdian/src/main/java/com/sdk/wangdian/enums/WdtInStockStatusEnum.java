package com.sdk.wangdian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 旺店通其他入库单状态枚举
 * @date 2024-07-08
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum WdtInStockStatusEnum {

    CANCEL("10", "已取消"),
    EDIT_ING("20", "编辑中"),
    WAIT_APPROVE("30", "待审核/待处理"),
    WAIT_CHECK("37", "待质检"),
    WAIT_CONFIRM("40", "质检待确认"),
    FINISH("80", "已完成");

    private String no;
    private String name;

    public static String getName(String no){
        for (WdtInStockStatusEnum statusEnum : WdtInStockStatusEnum.values()) {
            if(statusEnum.getNo().equals(no)){
                return statusEnum.getName();
            }
        }
        return "";
    }
}
