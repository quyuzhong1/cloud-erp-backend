package com.common.business.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 业务编码枚举
 * @author will
 * @date 2025/8/21 09:19
 */
public enum BusinessNoEnum implements EnumMessage {

    A("A", "A"),
    B("B", "B"),
    C("C", "C"),
    D("D", "D"),
    E("E", "E"),
    F("F", "F"),
    G("G", "G"),
    H("H", "H"),
    I("I", "I"),
    J("J", "J"),
    K("K", "K"),
    L("L", "L"),
    M("M", "M"),
    N("N", "N"),
    O("O", "O"),
    P("P", "P"),
    Q("Q", "Q"),
    R("R", "R"),
    S("S", "S"),
    T("T", "T"),
    U("U", "U"),
    V("V", "V"),
    W("W", "W"),
    X("X", "X"),
    Y("Y", "Y"),
    Z("Z", "Z"),
    ;


    @JsonValue
    @EnumValue
    private String code;

    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    BusinessNoEnum(String code, String name) {

        this.code = code;
        this.name = name;
    }

    public static String getNextCode(String currentCode) {
        //无编码直接返回A
        if (CharSequenceUtil.isBlank(currentCode)) {
            return A.code;
        }
        List<BusinessNoEnum> values = Arrays.asList(values());
        Optional<BusinessNoEnum> current = values.stream()
                .filter(e -> e.getCode().equals(currentCode))
                .findFirst();

        if (!current.isPresent()) {
            return null; // 未找到当前code
        }

        int currentIndex = values.indexOf(current.get());
        int nextIndex = (currentIndex + 1) % values.size(); // 循环处理
        return values.get(nextIndex).getCode();
    }
}
