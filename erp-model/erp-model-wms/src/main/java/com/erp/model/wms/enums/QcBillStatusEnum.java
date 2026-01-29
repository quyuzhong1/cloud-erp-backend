package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 质检单状态枚举
 *
 * @author Lambda
 * @Classname QcBillStatusEnum

 * @Date 2023-04-17 10:32
 * @Created by yl
 */
public enum QcBillStatusEnum implements EnumMessage {

    DRAFT("draft", "暂存"),
    WAIT_QC("waitQc", "待质检"),
    EXEMPTION("exemption", "免检"),
    FINISH_QC("finishQc", "已质检"),
    CANCEL("cancel", "取消"),
    WAIT_RE_QC("waitReQc", "待复检"),
    VOIDED("voided", "已作废"),
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


    QcBillStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static QcBillStatusEnum getByCode(String code) {

        QcBillStatusEnum[] eumnList = QcBillStatusEnum.values();
        for (QcBillStatusEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

}
