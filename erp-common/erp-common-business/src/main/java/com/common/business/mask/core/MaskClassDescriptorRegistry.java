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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类元数据注册中心：懒加载 + 缓存
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
     * 配置表 Redis cache-aside 访问器（可空：未配置或 sys 服务暂不可用时也能正常运行）
     */
    @Autowired(required = false)
    private CfgMaskFieldLocalCache configCache;

    /**
     * 获取（或懒加载）指定类的脱敏元数据
     *
     * <p>用 {@link ConcurrentHashMap#computeIfAbsent} 保证对同一 {@code clazz} 的并发首次访问只 scan 一次，
     * 避免多个线程同时启动时（典型场景：网关首批请求并发到达）对同一 POJO 类重复反射扫描浪费 CPU。</p>
     *
     * <p>scan 内部已避免锁住 cache 的整张表，对其他类的访问不会被阻塞。</p>
     */
    public MaskClassDescriptor of(Class<?> clazz) {
        if (clazz == null) {
            return MaskClassDescriptor.NO_MASK;
        }
        if (configCache != null && configCache.refreshIfNeeded()) {
            clear();
        }
        return cache.computeIfAbsent(clazz, this::scan);
    }

    /**
     * 整体清空缓存
     *
     * <p><b>慎用</b>：会让所有 POJO 类下次访问都重走反射扫描，业务高峰期相当于"全集群冷启动"。
     * 仅用于运维手动 refresh 接口或单元测试。</p>
     *
     * <p>配置变更场景请使用 {@link #evictByClassNames(Set)} 做增量失效。</p>
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 增量失效：只清掉受影响的类
     *
     * <p>用于 {@code cfg_mask_field} 配置变更后通知本注册中心：
     * 一次 add/update/delete 通常只影响 1~2 个类，没必要把全集群所有 POJO 元数据全清。</p>
     *
     * <p>实现：遍历当前 {@code cache.keySet()} 找到 {@code className} 命中的项移除。
     * 时间复杂度 O(N)（N=已扫描类数），N 通常在百~千级，远低于一次反射重扫的代价。</p>
     *
     * @param classNames 需要失效的类全限定名集合，null/空则什么都不做
     */
    public void evictByClassNames(Set<String> classNames) {
        if (classNames == null || classNames.isEmpty()) {
            return;
        }
        cache.keySet().removeIf(c -> classNames.contains(c.getName()));
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
            List<MaskFieldDescriptor> descList = buildFieldDescriptors(clazz, field);
            if (!descList.isEmpty()) {
                field.setAccessible(true);
                descriptors.addAll(descList);
            }
        }
        return descriptors.isEmpty() ? MaskClassDescriptor.NO_MASK : new MaskClassDescriptor(descriptors);
    }

    private List<MaskFieldDescriptor> buildFieldDescriptors(Class<?> ownerClass, Field field) {
        Mask anno = field.getAnnotation(Mask.class);
        List<CfgMaskFieldSnapshotEntry> configs = configCache != null
                ? configCache.getRules(ownerClass.getName(), field.getName())
                : Collections.emptyList();
        boolean hasAnno = anno != null;
        boolean hasConfig = configs != null && !configs.isEmpty();

        if (!hasAnno && !hasConfig) {
            // 无显式脱敏配置：判断是不是嵌套容器（需要继续向下递归）
            if (isContainerType(field)) {
                return Collections.singletonList(new MaskFieldDescriptor(field, MaskStrategy.AUTO_FROM_CONFIG, "", "***", "",
                        true, true, false, true));
            }
            return Collections.emptyList();
        }

        boolean container = isContainerType(field);
        if (hasConfig) {
            List<MaskFieldDescriptor> result = new ArrayList<>(configs.size());
            for (CfgMaskFieldSnapshotEntry config : configs) {
                result.add(new MaskFieldDescriptor(field, config.getStrategy(), config.getRegex(),
                        config.getReplacement(), config.getPermission(),
                        hasAnno ? anno.recursive() : true,
                        hasAnno ? anno.keepEmpty() : true,
                        config.isHideWhenMasked(), container,
                        config.getSort() == null ? 0 : config.getSort(),
                        config.isValueProtectEnabled(),
                        config.getProtectTableName(),
                        config.getProtectRecordIdColumn(),
                        config.getProtectValueColumn(),
                        config.getProtectDeletedColumn(),
                        config.getProtectMode(),
                        config.getProtectParamBindings()));
            }
            return result;
        }

        return Collections.singletonList(new MaskFieldDescriptor(field, anno.strategy(), anno.regex(),
                anno.replacement(), anno.permission(), anno.recursive(), anno.keepEmpty(),
                anno.hideWhenMasked(), container));
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
