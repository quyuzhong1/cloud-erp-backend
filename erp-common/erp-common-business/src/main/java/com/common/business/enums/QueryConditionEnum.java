package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@ToString
public enum QueryConditionEnum implements EnumMessage {

    GT("gt",">", "大于"),
    GE("ge",">=", "大于等于"),
    LT("lt","<", "小于"),
    LE("le","<=", "小于等于"),
    EQ("eq","=", "等于"),
    NE("ne","!=", "不等于"),
    IN_LIST("inList","in", "在...列表"),
    NOT_IN_LIST("notInList","not in", "不在...列表"),
    STARTS_WITH("startsWith", "starts_with", "开头以..."),
    ENDS_WITH("endsWith", "ends_with", "结尾以..."),
    CONTAINS("contains","ilike", "包含"),
    NOT_CONTAINS("notContains","not ilike", "不包含"),
    IS_NULL("isNull","is null", "为空"),
    NOT_NULL("notNull","is not null", "不为空"),
    BETWEEN("between","between", "介于"),
    ;

    public static final Map<String,QueryConditionEnum> CODE_MAPS;

    //不需要处理val的比较符
    public static final Set<QueryConditionEnum> SET_DIRECT_VAL = new HashSet<>();

    //需要处理成IN查询的比较符
    public static final Set<QueryConditionEnum> SET_IN = new HashSet<>();

    //需要处理成LIKE查询的比较符
    public static final Set<QueryConditionEnum> SET_LIKE = new HashSet<>();

    public static final Set<QueryConditionEnum> SET_NO_VAL = new HashSet<>();

    static {
        CODE_MAPS = Arrays.stream(QueryConditionEnum.values()).collect(Collectors.toMap(QueryConditionEnum ::getCompareCode,Function.identity()));
        SET_DIRECT_VAL.add(GT);
        SET_DIRECT_VAL.add(GE);
        SET_DIRECT_VAL.add(LT);
        SET_DIRECT_VAL.add(LE);
        SET_DIRECT_VAL.add(EQ);
        SET_DIRECT_VAL.add(NE);
        SET_IN.add(IN_LIST);
        SET_IN.add(NOT_IN_LIST);
        SET_LIKE.add(STARTS_WITH);
        SET_LIKE.add(ENDS_WITH);
        SET_LIKE.add(CONTAINS);
        SET_LIKE.add(NOT_CONTAINS);
        SET_NO_VAL.add(IS_NULL);
        SET_NO_VAL.add(NOT_NULL);
    }

    private final String code;

    private final String compareCode;

    private final String name;

    QueryConditionEnum(String compareCode,String code, String name) {
        this.compareCode = compareCode;
        this.code = code;
        this.name = name;
    }

}
