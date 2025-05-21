package com.common.business.enums;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/20 12:21
 */

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-20
 *@Description:
 *@Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum ProcessFormEvent implements EnumMessage {
    FS_PROCESS_FORM("FS_PROCESS_FORM","飞书流程定义Form解析"),
    DEFAULT("DEFAULT", "默认");
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
}
