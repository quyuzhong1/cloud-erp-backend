package com.common.core.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.enums.ApiError;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Classname: EnumCacheUtils
 * @Description: 枚举类缓存，并转换成下拉接口供前端使用，统一提供，为确保数据隔离，每个服务单独读取该服务的枚举类
 * 请求路径如/api/sys/common/enumDropDown?type=FieldFormatPatternType
 * @CreateTime: 2023-04-13  17:57
 * @Author: zhangchunlin
 */
@Slf4j
public class EnumCacheUtils {

    // 为保证可见性和有序性，防止出现半初始化
    private static volatile EnumCacheUtils INSTANCE;

    // 扫描的公共包路径
    private static final String[] SCAN_COMMON_ENUM_PACKAGE_PATH = {"com.common.business.enums", "com.common.core.enums"};

    // 服务内枚举包
    private static String SERVICE_ENUM_PACKAGE_PATH = "";

    // 枚举缓存
    private static volatile Map<String,List<Map<String,Object>>> enumMaps = new ConcurrentHashMap();

    private static final Lock lock = new ReentrantLock();

    private static final String SHOW_CODE = "code";

    private static final String SHOW_VALUE = "value";

    private static final String ENUM_GET_CODE = "getCode";

    private static final String ENUM_GET_NAME = "getName";

    public void setServiceEnumPackagePath(String serviceEnumPackagePath) {
        SERVICE_ENUM_PACKAGE_PATH = serviceEnumPackagePath;
    }

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
                    INSTANCE = new EnumCacheUtils();
                    return INSTANCE;
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
            Arrays.asList(SCAN_COMMON_ENUM_PACKAGE_PATH).forEach(enumPackagePath-> {
                log.info("扫描包{}下面的所有枚举类", enumPackagePath);
                searchClazzSets.addAll(ClassUtil.scanPackage(enumPackagePath,(clazz)-> clazz.isEnum()));
            });
            if(CharSequenceUtil.isNotBlank(SERVICE_ENUM_PACKAGE_PATH)) {
                log.info("扫描包{}下面的所有枚举类", SERVICE_ENUM_PACKAGE_PATH);
                searchClazzSets.addAll(ClassUtil.scanPackage(SERVICE_ENUM_PACKAGE_PATH,(clazz)-> clazz.isEnum()));
            }
            // 遍历所有的枚举类
            if (searchClazzSets != null && searchClazzSets.size() > 0) {
                for (Class clazz : searchClazzSets) {
                    if(excludeScan.contains(clazz.getSimpleName())) {
                        continue;
                    }
                    // 获取某个具体枚举类的，只处理枚举类中包含code和name的枚举类
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

    private static boolean checkPermitEnum(Class tt) {
       Set<String> fieldNames = Arrays.asList(tt.getDeclaredMethods()).stream().map(Method::getName).collect(Collectors.toSet());
       return fieldNames.contains(ENUM_GET_CODE) && fieldNames.contains(ENUM_GET_NAME);
    }

    public static List<Map<String,Object>> getEnumValueByClass(Class tt) {
        List<Map<String,Object>> list = Lists.newArrayList();
        Object[] objects = tt.getEnumConstants();
        try {
            Method getCode = tt.getMethod(ENUM_GET_CODE);
            Method getName = tt.getMethod(ENUM_GET_NAME);
            Map<String, Object> map;
            for (Object object : objects) {
                map = new HashMap();
                map.put(SHOW_CODE, getCode.invoke(object));
                map.put(SHOW_VALUE, getName.invoke(object));
                list.add(map);
            }
        } catch (Exception e) {
            log.error("获取枚举类{}信息异常", tt.getName(), e);
        }
        return list;
    }

}