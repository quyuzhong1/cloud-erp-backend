package com.common.core.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/7 10:11
 */
public interface EnumMessage {

    Object getCode();
    String getName();
    
    static Map<Class<? extends EnumMessage>, Map<Object, EnumMessage>> codeEnumMessageMaps = new HashMap<>();
    static Map<Class<? extends EnumMessage>, Map<String, EnumMessage>> nameEnumMessageMaps = new HashMap<>();

    // 默认方法，通过code获取枚举实例
    static <T extends EnumMessage> T getByCode(Class<T> enumType, Object code) {
    	Map<Object, EnumMessage> codeEnumMessageMap = codeEnumMessageMaps.get(enumType);
    	if(codeEnumMessageMap == null) {
    		codeEnumMessageMap = new HashMap<>();
    		for (T enumValue : enumType.getEnumConstants()) {
    			codeEnumMessageMap.put(enumValue.getCode(), enumValue);
            }
    		codeEnumMessageMaps.put(enumType, codeEnumMessageMap);
    	}
        return (T) codeEnumMessageMap.get(code);
    }
    
    // 默认方法，通过code获取name实例
    static <T extends EnumMessage> String getNameByCode(Class<T> enumType, Object code) {
    	T enumMessage = getByCode(enumType, code);
    	if(enumMessage != null) {
    		return enumMessage.getName();
    	}
        return null;
    }

    // 默认方法，通过name获取枚举实例
    static <T extends EnumMessage> T getByName(Class<T> enumType, String name) {
    	Map<String, EnumMessage> nameEnumMessageMap = nameEnumMessageMaps.get(enumType);
    	if(nameEnumMessageMap == null) {
    		nameEnumMessageMap = new HashMap<>();
    		for (T enumValue : enumType.getEnumConstants()) {
    			nameEnumMessageMap.put(enumValue.getName(), enumValue);
            }
    		nameEnumMessageMaps.put(enumType, nameEnumMessageMap);
    	}
        return (T) nameEnumMessageMap.get(name);
    }
    
    // 默认方法，通过name获取code实例
    static <T extends EnumMessage> Object getCodeByName(Class<T> enumType, String name) {
    	T enumMessage = getByName(enumType, name);
    	if(enumMessage != null) {
    		return enumMessage.getCode();
    	}
    	return null;
    }
}
