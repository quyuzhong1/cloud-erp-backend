package com.common.core.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * @author Will
 * @version 1.0
 * @description: 字段类型枚举
 * @date 2023/2/14 16:33
 */
@Getter
public enum FieldFormatPatternTypeEnum implements EnumMessage {

    ENUM_INTEGER("integer", "整数","^-?\\d+$"),
    ENUM_POSITIVE_INTEGER("positive_integer", "正整数","^[1-9]\\d*$"),
    ENUM_NUMBER("number", "数字","^-?\\d+(\\.\\d+)?$"),
    ENUM_DECIMAL("decimal", "小数","^-?\\d+\\.\\d+$"),
    ENUM_NUMBER_LETTER("number_letter", "数字和字母","^[a-z0-9A-Z]+$"),
    ENUM_AMOUNT("amount", "金额","^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,4})?$"),
    ENUM_DATE("date", "日期","(?:[0-9]{1,4}(?<!^0?0?0?0))\\/(?:0?[1-9]|1[0-2])\\/(?:0?[1-9]|1[0-9]|2[0-8]|(?:(?<=-(?:0?[13578]|1[02])-)(?:29|3[01]))|(?:(?<=-(?:0?[469]|11)-)(?:29|30))|(?:(?<=(?:(?:[0-9]{0,2}(?!0?0)(?:[02468]?(?<![13579])[048]|[13579][26]))|(?:(?:[02468]?[048]|[13579][26])00))-0?2-)(?:29)))"),
    ENUM_YEAR_MONTH("year_month", "年-月","^\\d{4}-((0([1-9]))|(1(0|1|2)))$"),
    ENUM_MOBILE("mobile", "手机","^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$"),
    ENUM_TELEPHONE("telephone", "电话","0\\d{2,3}-\\d{7,8}"),
    ENUM_QQ("QQ", "QQ","[1-9][0-9]{4,}"),
    ENUM_MAILBOX("mailbox", "邮箱","^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$"),
    ENUM_POSTAL_CODE("postal_code", "邮政编码","[1-9]/d{5}(?!/d)"),
    ENUM_ID("ID", "身份证","/d{15}|/d{18}"),
    ENUM_HTML("HTML", "HTML","<(/S*?)[^>]*>.*?<//1>|<.*? />"),
    ENUM_IP("IP", "IP","/d+/./d+/./d+/./d+");

    public static final String INTEGER = "整数";
    public static final String POSITIVEINTEGER = "正整数";
    public static final String NUMBER = "数字";
    public static final String DECIMAL = "小数";
    public static final String NUMBER_LETTER = "数字和字母";
    public static final String AMOUNT = "金额";
    public static final String DATE = "日期";
    public static final String YEARMONTH = "年-月";
    public static final String MOBILE = "手机";
    public static final String TELEPHONE = "电话";
    public static final String QQ = "QQ";
    public static final String MAILBOX = "邮箱";
    public static final String POSTAL_CODE = "邮政编码";
    public static final String ID = "身份证";
    public static final String HTML = "HTML";
    public static final String IP = "IP";

    private String code;

    private String name;

    private String desc;

    FieldFormatPatternTypeEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;

    }

    public static FieldFormatPatternTypeEnum getByCode(String code) {
        FieldFormatPatternTypeEnum[] values = values();
        for (FieldFormatPatternTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static FieldFormatPatternTypeEnum getByName(String name) {
        FieldFormatPatternTypeEnum[] values = values();
        for (FieldFormatPatternTypeEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}

