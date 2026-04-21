package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 推送任务记录 推送状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpOutputTaskRecordStatusEnum implements EnumMessage {
	INIT("init", "待推送"),
	MQSUCCESS("mqsuccess", "mq推送成功"),
	MQERROR("mqerror", "mq推送失败"),
    // 消费失败
	COSUMERERROR("cosumererror", "系统重试中"),
    FINISH("finish", "推送成功"),
    // 推送失败
	ERROR("error", "待人工处理"),
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

    DmpOutputTaskRecordStatusEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DmpOutputTaskRecordStatusEnum statusEnum : DmpOutputTaskRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
