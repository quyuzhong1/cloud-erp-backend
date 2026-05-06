package com.common.core.utils;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SqlUtils {

    /**
     * 校验SQL合法性，是否有注入风险
     * @param input
     * @return false 表示不通过
     */
    public static boolean verifySqlLegality(String input){
        String pattern = "(?i)(insert|delete|update|select|drop|union|truncate|grant|exec|alter|create|rename|replace|shutdown|restore|backup|attach|detach|declare|execute)\\s";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(input);
        return !matcher.find();
    }
    
    public static void appendPermissionSql(StringBuilder sqlString , String tableAliaField , List<String> permissionDataList) {
        List<String> permissionList = cleanPermissionDataList(permissionDataList);
    	if(CollUtil.isNotEmpty(permissionList)) {
    		sqlString.append(" ( ");
    		sqlString.append(tableAliaField);
    		sqlString.append(" in (");
    		sqlString.append(permissionList.stream().map(SqlUtils::toSqlLiteral).collect(Collectors.joining(",")));
    		sqlString.append(" ) ) ");
    	}
    }

    public static void appendBlankOrInPermissionSql(StringBuilder sqlString, List<String> tableAliaFieldList, List<String> permissionDataList) {
        if (CollUtil.isEmpty(tableAliaFieldList)) {
            return;
        }
        List<String> fieldList = tableAliaFieldList.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        List<String> permissionList = cleanPermissionDataList(permissionDataList);
        if (CollUtil.isEmpty(fieldList)) {
            return;
        }
        List<String> permissionListWithBlank = new ArrayList<>();
        permissionListWithBlank.add("");
        if (CollUtil.isNotEmpty(permissionList)) {
            permissionListWithBlank.addAll(permissionList);
        }
        sqlString.append(" AND (");
        for (int i = 0; i < fieldList.size(); i++) {
            if (i > 0) {
                sqlString.append(" OR ");
            }
            sqlString.append(" ( ");
            sqlString.append(fieldList.get(i));
            sqlString.append(" in (");
            sqlString.append(permissionListWithBlank.stream().map(SqlUtils::toSqlLiteral).collect(Collectors.joining(",")));
            sqlString.append(" ) ) ");
        }
        sqlString.append(" )");
    }

    private static List<String> cleanPermissionDataList(List<String> permissionDataList) {
        if (CollUtil.isEmpty(permissionDataList)) {
            return permissionDataList;
        }
        return permissionDataList.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private static String toSqlLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
