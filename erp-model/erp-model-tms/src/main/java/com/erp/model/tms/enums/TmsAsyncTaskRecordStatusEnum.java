package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 异步任务记录 状态 枚举
 * </p>
 *
 * @author jack
 * @since 2026-01-28 12:16:20
 */
public enum TmsAsyncTaskRecordStatusEnum implements EnumMessage {
    PENDING("pending", "待执行"),
    ING("ing", "进行中"),
	FAILED("failed", "失败"),
    FINISH("finish", "已完成"),
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

    TmsAsyncTaskRecordStatusEnum(String code, String name) {
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
        for (TmsAsyncTaskRecordStatusEnum statusEnum : TmsAsyncTaskRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(TmsAsyncTaskRecordStatusEnum.values()).map(TmsAsyncTaskRecordStatusEnum::getCode).collect(Collectors.toList());
    }

    public static TmsAsyncTaskRecordStatusEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }
}
