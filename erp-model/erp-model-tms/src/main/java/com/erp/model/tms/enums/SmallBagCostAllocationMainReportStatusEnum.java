package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 小包费用分摊主表 核算状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-12-05 15:14:47
 */
public enum SmallBagCostAllocationMainReportStatusEnum implements EnumMessage {
	TOBECONFIRM("toBeConfirm", "待确认"),
	CONFIRMED("confirmed", "已确认"),
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

    SmallBagCostAllocationMainReportStatusEnum(String code, String name) {
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
        for (SmallBagCostAllocationMainReportStatusEnum statusEnum : SmallBagCostAllocationMainReportStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
