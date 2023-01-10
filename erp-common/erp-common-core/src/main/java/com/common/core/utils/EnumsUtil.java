package com.common.core.utils;

import com.common.core.constant.EnumMessage;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**根据值和枚举类型可以获得枚举对象。
 * */
public class EnumsUtil {

    /**
     * 存放单个枚举对象 map常量定义
     */
    private static Map<Integer, EnumMessage> SINGLE_ENUM_MAP;


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
    public static <T extends EnumMessage>  T getEnumObject(Integer value, Class<?> clazz){
        try {
            initialSingleEnumMap(clazz);
        } catch (Exception e){
            throw new ServiceException(ApiError.ERROR_9028);
        }
        T retobj= (T)SINGLE_ENUM_MAP.get(value);

        return retobj;
    }

}
