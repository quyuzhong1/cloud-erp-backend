package com.common.core.utils;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.collection.CollUtil;

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
    	if(CollUtil.isNotEmpty(permissionDataList)) {
    		sqlString.append(" (");
    		int i = 0;
    		int size = permissionDataList.size();
    		for(String permissionData : permissionDataList) {
    			sqlString.append(tableAliaField);
    			sqlString.append(" LIKE '%");
    			sqlString.append(permissionData);
    			sqlString.append("%' ");
    			i = i + 1;
    			if(i < size) {
    				sqlString.append(" OR ");
    			}
    		}
    		sqlString.append(" ) ");
    	}
    }
}
