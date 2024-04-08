package com.common.business.threadlocal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.business.enums.ServiceCodeNameEnum;

public class DictThreadLocal {
    
    private static ThreadLocal<Map<String, List<Map<String, Object>>>> dictThread = new ThreadLocal<>();
    
    public static void set(String code, String text, String table, String key , ServiceCodeNameEnum serviceCodeNameEnum , List<Map<String, Object>> data){
    	Map<String, List<Map<String, Object>>> threadMapData = dictThread.get();
    	if(threadMapData == null) {
    		threadMapData = new HashMap<>();
    	}
    	threadMapData.put(getDataKey(code, text, table, key , serviceCodeNameEnum), data);
        dictThread.set(threadMapData);
    }
    
    public static List<Map<String, Object>> get(String code, String text, String table, String key , ServiceCodeNameEnum serviceCodeNameEnum){
        Map<String, List<Map<String, Object>>> map = dictThread.get();
        if(map != null) {
        	return map.get(getDataKey(code, text, table, key , serviceCodeNameEnum));
        }
		return null;
    }
    
    //防止内存泄漏
    public static void remove(){
        dictThread.remove();
    }
    
    public static String getDataKey(String code, String text, String table, String key , ServiceCodeNameEnum serviceCodeNameEnum) {
    	return code + "_" + text + "_" + table + "_" + key + "_" + serviceCodeNameEnum.getCode();
    }
}