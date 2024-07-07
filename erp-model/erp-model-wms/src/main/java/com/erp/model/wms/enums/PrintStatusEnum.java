package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打印状态
 * @date 2024-07-05
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum PrintStatusEnum implements EnumMessage {
    NOT_PRINT("notPrint", "未打印"),
    PRINT_ING("printIng", "打印中"),
    PRINT_FINISH("printFinish", "已打印")
    ;

    private String code;
    private String name;
}
