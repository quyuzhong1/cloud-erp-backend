package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 报关明细中间表生成状态
 *
 * @author jack
 * @date 2026-04-29
 */
public enum DeliveryDeclareDetailMidGenerateStatusEnum implements EnumMessage {

    WAIT("wait", "未生成"),
    FINISH("finish", "已生成"),
    ;
    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    DeliveryDeclareDetailMidGenerateStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取状态编码
     *
     * @return 状态编码
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 根据编码获取状态名称
     *
     * @param code 状态编码
     * @return 状态名称
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DeliveryDeclareDetailMidGenerateStatusEnum item : DeliveryDeclareDetailMidGenerateStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
