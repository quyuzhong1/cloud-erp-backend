package com.common.business.mask.core;

import com.common.business.mask.Mask;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.utils.ConvertUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类元数据注册中心：懒加载 + 永久缓存
 *
 * <p>关键性能优化：</p>
 * <ul>
 *   <li>{@link MaskClassDescriptor#NO_MASK} 哨兵：扫描发现无任何脱敏字段且无嵌套容器的类直接缓存哨兵，
 *       下次 O(1) 返回</li>
 *   <li>框架类前缀直返 NO_MASK：跳过 java/spring/mybatis 等无业务字段的类</li>
 *   <li>{@link #clear()}：配置变更时整体失效，下次访问重建</li>
 * </ul>
 *
 * <p>合并规则（{@code cfg_mask_field} 配置表 vs {@link Mask} 注解）：</p>
 * <ul>
 *   <li>同一 {@code (className, fieldName)} 在配置表存在记录 → 配置表覆盖注解</li>
 *   <li>仅注解存在 → 用注解</li>
 *   <li>仅配置表存在 → 用配置表（即使字段无注解也会被脱敏）</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Component
public class MaskClassDescriptorRegistry {

    /**
     * 框架类前缀白名单：直接缓存为 NO_MASK，避免对 IPage/Page/HashMap 等做无意义反射
     */
    private static final String[] FRAMEWORK_PREFIXES = {
            "java.",
            "javax.",
            "sun.",
            "com.sun.",
            "org.springframework.",
            "org.apache.",
            "com.fasterxml.",
            "com.baomidou.mybatisplus.",
            "io.swagger.",
            "io.netty.",
            "feign.",
            "lombok.",
    };

    private final ConcurrentHashMap<Class<?>, MaskClassDescriptor> cache = new ConcurrentHashMap<>(256);

    /**
     * 配置表本地快照（可空：未启用配置或 sys 服务暂不可用时也能正常运行）
     */
    @Autowired(required = false)
    private CfgMaskFieldLocalCache configCache;

    /**
     * 获取（或懒加载）指定类的脱敏元数据
     */
    public MaskClassDescriptor of(Class<?> clazz) {
        if (clazz == null) {
            return MaskClassDescriptor.NO_MASK;
        }
        MaskClassDescriptor cached = cache.get(clazz);
        if (cached != null) {
            return cached;
        }
        MaskClassDescriptor built = scan(clazz);
        cache.put(clazz, built);
        return built;
    }

    /**
     * 整体清空缓存（配置变更后由 {@link CfgMaskFieldLocalCache#apply} 触发调用）
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 当前缓存条目数（运维视图用）
     */
    public int size() {
        return cache.size();
    }

    private MaskClassDescriptor scan(Class<?> clazz) {
        if (isFrameworkClass(clazz)) {
            return MaskClassDescriptor.NO_MASK;
        }
        Field[] all = ConvertUtils.getAllFields(clazz);
        if (all == null || all.length == 0) {
            return MaskClassDescriptor.NO_MASK;
        }
        List<MaskFieldDescriptor> descriptors = new ArrayList<>();
        for (Field field : all) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod)) {
                continue;
            }
            MaskFieldDescriptor desc = buildFieldDescriptor(clazz, field);
            if (desc != null) {
                field.setAccessible(true);
                descriptors.add(desc);
            }
        }
        return descriptors.isEmpty() ? MaskClassDescriptor.NO_MASK : new MaskClassDescriptor(descriptors);
    }

    private MaskFieldDescriptor buildFieldDescriptor(Class<?> ownerClass, Field field) {
        Mask anno = field.getAnnotation(Mask.class);
        CfgMaskFieldSnapshotEntry config = configCache != null
                ? configCache.get(ownerClass.getName(), field.getName())
                : null;
        boolean hasAnno = anno != null;
        boolean hasConfig = config != null;

        if (!hasAnno && !hasConfig) {
            // 无显式脱敏配置：判断是不是嵌套容器（需要继续向下递归）
            if (isContainerType(field)) {
                return new MaskFieldDescriptor(field, MaskStrategy.AUTO_FROM_CONFIG, "", "***", "",
                        true, true, false, true);
            }
            return null;
        }

        // 配置表覆盖注解
        MaskStrategy strategy;
        String regex;
        String replacement;
        String permission;
        boolean recursive;
        boolean keepEmpty;
        boolean hideWhenMasked;
        if (hasConfig) {
            strategy = config.getStrategy();
            regex = config.getRegex();
            replacement = config.getReplacement();
            permission = config.getPermission();
            recursive = hasAnno ? anno.recursive() : true;
            keepEmpty = hasAnno ? anno.keepEmpty() : true;
            hideWhenMasked = config.isHideWhenMasked();
        } else {
            strategy = anno.strategy();
            regex = anno.regex();
            replacement = anno.replacement();
            permission = anno.permission();
            recursive = anno.recursive();
            keepEmpty = anno.keepEmpty();
            hideWhenMasked = anno.hideWhenMasked();
        }

        boolean container = isContainerType(field);
        return new MaskFieldDescriptor(field, strategy, regex, replacement, permission,
                recursive, keepEmpty, hideWhenMasked, container);
    }

    /**
     * 是否为"嵌套对象 / 集合 / Map / 数组" —— 即使本身无 @Mask 也要继续向下递归
     */
    private boolean isContainerType(Field field) {
        Class<?> type = field.getType();
        if (type.isPrimitive() || type.isEnum()) {
            return false;
        }
        if (type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class
                || type == Character.class || java.util.Date.class.isAssignableFrom(type)
                || type.getName().startsWith("java.time.")) {
            return false;
        }
        if (type.isArray()) {
            Class<?> comp = type.getComponentType();
            return !(comp.isPrimitive() || comp == String.class);
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            // 元素类型如果是 String/基本包装则不视为需要递归
            Type generic = field.getGenericType();
            if (generic instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) generic).getActualTypeArguments();
                Type element = Map.class.isAssignableFrom(type) && args.length >= 2 ? args[1] : (args.length > 0 ? args[0] : null);
                if (element instanceof Class) {
                    Class<?> ec = (Class<?>) element;
                    if (ec == String.class || Number.class.isAssignableFrom(ec) || ec == Boolean.class) {
                        return false;
                    }
                }
            }
            return true;
        }
        return !isFrameworkClass(type);
    }

    private boolean isFrameworkClass(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        String name = clazz.getName();
        for (String prefix : FRAMEWORK_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 仅用于运维 / 单元测试：返回当前已扫描类列表的快照
     */
    public Set<Class<?>> snapshotKeys() {
        return new HashSet<>(cache.keySet());
    }

    /**
     * 仅用于单元测试：手工注入一个类描述符（绕过反射扫描）
     */
    void putForTest(Class<?> clazz, MaskClassDescriptor descriptor) {
        cache.put(clazz, descriptor);
    }

    /**
     * 仅用于单元测试：批量删除指定类的缓存
     */
    void evict(Collection<Class<?>> classes) {
        if (classes == null) {
            return;
        }
        cache.keySet().removeAll(new HashSet<>(classes));
    }

    /**
     * 仅用于单元测试：返回当前框架类前缀（避免外部修改）
     */
    static String[] frameworkPrefixesForTest() {
        return Arrays.copyOf(FRAMEWORK_PREFIXES, FRAMEWORK_PREFIXES.length);
    }
}
