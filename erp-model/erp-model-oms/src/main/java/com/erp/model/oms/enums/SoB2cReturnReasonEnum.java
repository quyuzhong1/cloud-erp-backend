package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum SoB2cReturnReasonEnum implements EnumMessage {
    COLOR("COLOR","COLOR","platform"),
    DEFECTIVE("DEFECTIVE","DEFECTIVE","platform"),
    NOT_AS_DESCRIBED("NOT_AS_DESCRIBED","NOT_AS_DESCRIBED","platform"),
    OTHER("OTHER","OTHER","platform"),
    SIZE_TOO_LARGE("SIZE_TOO_LARGE","SIZE_TOO_LARGE","platform"),
    SIZE_TOO_SMALL("SIZE_TOO_SMALL","SIZE_TOO_SMALL","platform"),
    STYLE("STYLE","STYLE","platform"),
    UNKNOWN("UNKNOWN","UNKNOWN","platform"),
    UNWANTED("UNWANTED","UNWANTED","platform"),
    WRONG_ITEM("WRONG_ITEM","WRONG_ITEM","platform"),
    DESCRIPTION_NOT_MATCH("DESCRIPTION_NOT_MATCH","描述不符","erp"),
    QUALITY_ISSUES("qualityProblem","质量问题","erp"),
    ORDER_ERROR("ORDER_ERROR","下单错误","erp"),
    OTHER_ERP("other","其他","erp"),
    ;

    SoB2cReturnReasonEnum(String code, String name,String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public final String code;
    /**
     * 名称
     */
    private final String name;

    @Getter
    private final String type;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 通过code查询
     */
    public static SoB2cReturnReasonEnum getByCode(String code){
        return Stream.of(SoB2cReturnReasonEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cReturnReasonEnum typeEnum : SoB2cReturnReasonEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }

    public static List<SoB2cReturnReasonEnum> listByType(String type) {
        if (StringUtils.isBlank(type)) {
            return new ArrayList<>();
        }
        return Arrays.stream(SoB2cReturnReasonEnum.values()).filter(v->v.getType().equals(type)).collect(Collectors.toList());
    }
}
