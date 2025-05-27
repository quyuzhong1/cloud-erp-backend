package com.erp.model.sys.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleTypeEnum implements EnumMessage {
    CFG_THIRD_NOTICE("cfgThirdNotice", "三方通知配置"),
    ;

    private final String code;
    private final String name;
}
