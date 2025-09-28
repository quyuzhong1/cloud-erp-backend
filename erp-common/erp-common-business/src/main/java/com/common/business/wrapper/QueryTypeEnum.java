package com.common.business.wrapper;

public enum QueryTypeEnum {
    EQ("eq" , "等于"),
    GROUP_BY("groupBy" , "分组"),
    IN("in" , "在..里面"),
    NOT_IN("notIn" , "不在..里面"),
    ORDER_BY_ASC("orderByAsc" , "升序"),
    ORDER_BY_DESC("orderByDesc"  , "降序"),
    APPLY("apply" , "拼接sql"),
    LAST("last" , "末尾拼接sql"),
    IS_NULL("isNull" , "为空"),
    IS_NOT_NULL("isNotNull" , "不为空"),
    NE("ne" , "不等于"),
    LE("le" , "小于等于"),
    GE("ge" , "大于等于"),
    LT("lt" , "小于"),
    GT("gt"  , "大于"),
    LIKE("like"  , "匹配"),
    NOT_LIKE("notLike"  , "不匹配"),
    SELECT("select"  , "查询"),
    ;
    private String value;
    
    private String label;

    QueryTypeEnum(String value , String label) {
        this.value = value;
        this.label = label;
    }

    public String value() {
        return value;
    }
    
    public String getLabel() {
        return label;
    }

    public String getValue() {
		return value;
	}

    public static QueryTypeEnum getByValue(String value) {
    	for (QueryTypeEnum queryTypeEnum : QueryTypeEnum.values()) {
			if (queryTypeEnum.value.equals(value)) {
				return queryTypeEnum;
			}
		}
    	return null;
    }

}
