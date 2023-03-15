package com.common.core.enums;

import lombok.Getter;

/**
 * @author Will
 * @version 1.0
 * @description: 字段类型枚举
 * @date 2023/2/14 16:33
 */
@Getter
public enum FieldFormatPatternTypeEnum  {

    ENUM_INTEGER("integer", "整数","^-?\\d+$"),
    ENUM_POSITIVE_INTEGER("positive_integer", "正整数","^[1-9]\\d*$"),
    ENUM_NUMBER("number", "数字","^-?\\d+(\\.\\d+)?$"),
    ENUM_DECIMAL("decimal", "小数","^-?\\d+\\.\\d+$"),
    ENUM_NUMBER_LETTER("number_letter", "数字和字母","^[a-z0-9A-Z]+$"),
    ENUM_NOT_CHINESE("not_chinese","非中文","[\\x00-\\xff]+"),
    ENUM_AMOUNT("amount", "金额","^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,4})?$"),
    ENUM_DATE("date", "日期","\\d{4}\\/([1-9]|0[1-9]|1[012])\\/([1-9]|0[1-9]|[12][0-9]|3[01])"),
    ENUM_YEAR_MONTH("year_month", "年-月","^\\d{4}-((0([1-9]))|(1(0|1|2)))$"),
    ENUM_MOBILE("mobile", "手机","^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$"),
    ENUM_TELEPHONE("telephone", "电话","0\\d{2,3}-\\d{7,8}"),
    ENUM_QQ("QQ", "QQ","[1-9][0-9]{4,}"),
    ENUM_MAILBOX("mailbox", "邮箱","^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$"),
    ENUM_POSTAL_CODE("postal_code", "邮政编码","[1-9]/d{5}(?!/d)"),
    ENUM_ID("ID", "身份证","/d{15}|/d{18}"),
    ENUM_HTML("HTML", "HTML","<(/S*?)[^>]*>.*?<//1>|<.*? />"),
    ENUM_IP("IP", "IP","/d+/./d+/./d+/./d+"),
    ENUM_URL ("url","网址","(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?|[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])"),
    ENUM_BANK_CARD_NO("bankCardNo","银行卡号","[1-9]/d{16}(?!/d)|");



    public static final String BANK_CARD_NO = "bankCardNo";
    public static final String URL = "url";
    public static final String INTEGER = "integer";
    public static final String POSITIVEINTEGER = "positive_integer";
    public static final String NUMBER = "number";
    public static final String DECIMAL = "decimal";
    public static final String NUMBER_LETTER = "number_letter";
    public static final String NOT_CHINESE = "not_chinese";
    public static final String AMOUNT = "amount";
    public static final String DATE = "date";
    public static final String YEARMONTH = "year_month";
    public static final String MOBILE = "mobile";
    public static final String TELEPHONE = "telephone";
    public static final String QQ = "QQ";
    public static final String MAILBOX = "mailbox";
    public static final String POSTAL_CODE = "postal_code";
    public static final String ID = "ID";
    public static final String HTML= "HTML";
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


    public static String getRegularByCode(String code) {
        FieldFormatPatternTypeEnum[] values = values();
        for (FieldFormatPatternTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.getDesc();
            }
        }
        return "";
    }
}

