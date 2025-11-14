package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jack
 * @version 1.0
 * @description:
 * @date 2025-11-12
 */
public enum SampleRecipientTabEnum implements EnumMessage {
    WAIT_SUBMIT("waitSubmit", "待提交"),
    WAIT_ME_APPROVE("approveIng", "待我审核"),
    NO_OUTSTOCK("noOutstock", "无需出库"),
    WAIT_OUTSTOCK("waitOutstock", "待出库"),
    COMPLETE_OUTSTOCK("completeOutstock", "已出库"),
    REJECT("reject", "不通过"),
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

    SampleRecipientTabEnum(String code, String name) {
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

    public static SampleRecipientTabEnum getByCode(String code) {

        SampleRecipientTabEnum[] enumList = SampleRecipientTabEnum.values();
        for (SampleRecipientTabEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (SampleRecipientTabEnum item : SampleRecipientTabEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
