package com.common.core.utils;

import cn.hutool.core.util.ClassUtil;
import com.common.core.enums.ApiError;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Classname: EnumCacheUtils
 * @Description: 枚举类缓存，并转换成下拉供前端使用，统一提供
 * @CreateTime: 2023-04-13  17:57
 * @Author: zhangchunlin
 */
@Slf4j
public class EnumCacheUtils {

    // 为保证可见性和有序性，防止出现半初始化
    private static volatile EnumCacheUtils INSTANCE;

    // 扫描的包路径
    private static final String[] SCAN_ENUM_PACKAGE_PATH = {"com.common.business.enums", "com.common.core.enums","com.erp.model"};

    // 枚举缓存
    private static volatile Map<String,List<Map<String,Object>>> enumMaps = new ConcurrentHashMap();

    private static final Lock lock = new ReentrantLock();

    private static final String ENUM_CODE_FIELD = "code";

    private static final String ENUM_VALUE_FIELD = "name";

    /**
     * 不扫描的枚举类
     */
    private static final List<String> excludeScan = new ArrayList<String>() {{
        add(ApiError.class.getSimpleName());
        add("ThirdPlatformEnums");
    }};

    /**
     * 获取单例
     * @return
     */
    public static EnumCacheUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (EnumCacheUtils.class) {
                if (INSTANCE == null) {
                    return new EnumCacheUtils();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 构造方法
     */
    private EnumCacheUtils(){

    }

    public Map<String,List<Map<String,Object>>> getData() {
        if(enumMaps != null && !enumMaps.isEmpty()) {
            return enumMaps;
        }
        lock.lock();
        try {
            // 读取某个包及子包下面的所有枚举类
            Set<Class<?>> searchClazzSets = Sets.newHashSet();
            Arrays.asList(SCAN_ENUM_PACKAGE_PATH).forEach(enumPackagePath-> searchClazzSets.addAll(ClassUtil.scanPackage(enumPackagePath,(clazz)-> clazz.isEnum())));
            // 遍历所有的枚举类
            if (searchClazzSets != null && searchClazzSets.size() > 0) {
                for (Class clazz : searchClazzSets) {
                    if(excludeScan.contains(clazz.getSimpleName())) {
                        continue;
                    }
                    // 获取某个具体枚举类的
                    // 只处理枚举类中包含code和name的枚举类
                    if(checkPermitEnum(clazz)) {
                        List<Map<String, Object>> list = getEnumValueByClass(clazz);
                        enumMaps.put(clazz.getSimpleName().replace("Enum","").replace("enum",""), list);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return enumMaps;
    }

    public static boolean checkPermitEnum(Class tt) {
       Set<String> fieldNames = Arrays.asList(tt.getFields()).stream().map(Field::getName).collect(Collectors.toSet());
       return fieldNames.contains(ENUM_CODE_FIELD) && fieldNames.contains(ENUM_VALUE_FIELD);
    }

    public static List<Map<String,Object>> getEnumValueByClass(Class tt) {
        List<Map<String,Object>> list = Lists.newArrayList();
        Object[] objects = tt.getEnumConstants();
        try {
            Method getCode = tt.getMethod("getCode");
            Method getName = tt.getMethod("getName");
            Map<String, Object> map;
            for (Object object : objects) {
                map = new HashMap();
                map.put("code", getCode.invoke(object));
                map.put("value", getName.invoke(object));
                list.add(map);
            }
        } catch (Exception e) {
            log.error("获取枚举类{}信息异常", tt.getName(), e);
        }
        return list;
    }

}