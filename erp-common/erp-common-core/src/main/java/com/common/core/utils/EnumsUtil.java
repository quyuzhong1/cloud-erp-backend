package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**根据值和枚举类型可以获得枚举对象。
 * */
public class EnumsUtil {

    /**
     * 存放单个枚举对象 map常量定义
     */
    private static Map<Object, EnumMessage> SINGLE_ENUM_MAP;


    /**静态初始化块*/
    static {

    }

    /**
     * 加载每个枚举对象数据
     * */
    private static void  initialSingleEnumMap(Class<?> cls ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SINGLE_ENUM_MAP = new LinkedHashMap<>();
        Method method = cls.getMethod("values");
        EnumMessage inter[] = (EnumMessage[]) method.invoke(null, null);
        for (EnumMessage enumMessage : inter) {
            SINGLE_ENUM_MAP.put(enumMessage.getCode(), enumMessage);
        }
    }


    /**
     * 获取value返回枚举对象
     * @param value
     * @param clazz */
    public static <T extends EnumMessage>  T getEnumObject(Object value, Class<?> clazz){
        try {
            initialSingleEnumMap(clazz);
        } catch (Exception e){
            throw new ServiceException(ApiError.ERROR_9028);
        }
        T retobj= (T)SINGLE_ENUM_MAP.get(value);
        if (ObjectUtils.isEmpty(retobj)) {
            retobj= (T)SINGLE_ENUM_MAP.get(String.valueOf(value));
        }

        return retobj;
    }

}
