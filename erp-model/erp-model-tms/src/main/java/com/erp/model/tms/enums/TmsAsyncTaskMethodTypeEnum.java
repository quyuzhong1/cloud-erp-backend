package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 异步任务方法类型枚举：在同一 business_type 下区分不同执行方法
 * </p>
 *
 * @author jack
 * @since 2026-05-30
 */
public enum TmsAsyncTaskMethodTypeEnum implements EnumMessage {
	PUSH_ALLOCATION("pushAllocation", "下推分摊"),
	UPDATE_REPORT_STATUS("updateReportStatus", "批量更新核算状态"),
	RE_ALLOCATION("reAllocation", "重新分摊"),
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

    TmsAsyncTaskMethodTypeEnum(String code, String name) {
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
        for (TmsAsyncTaskMethodTypeEnum methodTypeEnum : TmsAsyncTaskMethodTypeEnum.values()) {
            if (code.equals(methodTypeEnum.getCode())) {
                return methodTypeEnum.getName();
            }
        }
        return "";
    }
}
