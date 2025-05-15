package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 头程调整记录 调整字段 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-05-12 15:19:23
 */
public enum FirstMileChangeRecordCategoryFieldEnum implements EnumMessage {
	CURRENT_PERIOD_ALLOCATED_COST("current_period_allocated_cost", "本期分摊费用"),
	MID_PERIOD_TRANSIT_COST("mid_period_transit_cost", "冲期初在途费用"),
    END_PERIOD_TRANSIT_COST("end_period_transit_cost", "期末在途费用"),
	END_PERIOD_ESTIMATED_COST("end_period_estimated_cost", "期末暂估费用"),
	PRODUCT_WEIGHT("product_weight", "单产品重量"),
	CHARGED_WEIGHT("charged_weight", "出库重量"),
	BOX_LENGTH("box_length", "出库尺寸(长)"),
    BOX_WIDTH("box_width", "出库尺寸(宽)"),
    BOX_HEIGHT("box_height", "出库尺寸(高)"),
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

    FirstMileChangeRecordCategoryFieldEnum(String code, String name) {
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
        for (FirstMileChangeRecordCategoryFieldEnum statusEnum : FirstMileChangeRecordCategoryFieldEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
