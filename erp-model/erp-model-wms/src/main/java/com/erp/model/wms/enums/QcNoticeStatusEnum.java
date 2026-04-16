package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 质检单状态枚举
 *
 * @author Lambda
 * @Classname QcBillStatusEnum

 * @Date 2023-04-17 10:32
 * @Created by yl
 */
public enum QcNoticeStatusEnum implements EnumMessage {

    WAIT("wait", "待质检"),
    PART("part", "部分质检"),
    FINISH("finish", "已质检"),
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


    QcNoticeStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static QcNoticeStatusEnum getByCode(String code) {

        QcNoticeStatusEnum[] eumnList = QcNoticeStatusEnum.values();
        for (QcNoticeStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {

        QcNoticeStatusEnum[] eumnList = QcNoticeStatusEnum.values();
        for (QcNoticeStatusEnum item : eumnList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }

    public static List<String> getCodeList() {
        return Arrays.stream(QcNoticeStatusEnum.values()).map(QcNoticeStatusEnum::getCode).collect(Collectors.toList());
    }

}
