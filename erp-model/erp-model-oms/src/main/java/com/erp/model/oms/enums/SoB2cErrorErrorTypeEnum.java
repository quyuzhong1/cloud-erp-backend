package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * 【异常订单具体异常类型】
 *
 * @author Jim
 * @since 2024-05-21
 */
@Getter
@AllArgsConstructor
public enum SoB2cErrorErrorTypeEnum{
    MANUAL_SIGN_DELIVERY("manualSignDelivery", "手动发货标记销售平台发货异常", SoB2cErrorTypeEnum.SIGN_DELIVERY),
    FALSEHOOD_SIGN_DELIVERY("falsehoodSignDelivery", "虚假发货标记销售平台发货异常", SoB2cErrorTypeEnum.SIGN_DELIVERY),
    THIRD_WAREHOUSE_SIGN_DELIVERY("thirdWarehouseSignDelivery", "第三方出库仓标记销售平台发货异常", SoB2cErrorTypeEnum.SIGN_DELIVERY),


    ;

    /**
     * 异常代号
     */
    @EnumValue
    private final String code;
    /**
     * 异常名称
     */
    private final String name;
    /**
     * 所属的前端显示的异常信息
     */
    private final SoB2cErrorTypeEnum typeEnum;

    /**
     * 通过code查询
     * SoB2cErrorErrorTypeEnum
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        SoB2cErrorErrorTypeEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * SoB2cErrorErrorTypeEnum
     * 枚举
     */
    public static SoB2cErrorErrorTypeEnum getByCode(String code) {
        return Stream.of(SoB2cErrorErrorTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过code查询 默认:虚假发货
     * SoB2cErrorErrorTypeEnum
     * 枚举
     */
    public static SoB2cErrorErrorTypeEnum getByCodeNullDefault(String code) {
        return Stream.of(SoB2cErrorErrorTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(FALSEHOOD_SIGN_DELIVERY);
    }
}
