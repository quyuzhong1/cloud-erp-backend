package com.common.core.utils;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CollectionToMapUtil {

    private CollectionToMapUtil() {
    }

    /**
     * 根据指定key函数将集合转为Map，key唯一，重复时保留第一个。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @return Map<K, T>
     */
    public static <T, K> Map<K, T> indexBy(Collection<T> list, Function<T, K> keyFn) {
        return indexBy(list, keyFn, (a, b) -> a, LinkedHashMap::new);
    }

    /**
     * 根据指定key函数将集合转为Map，key唯一，重复时用mergeFn处理。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param mergeFn 冲突时的合并函数
     * @return Map<K, T>
     */
    public static <T, K> Map<K, T> indexBy(Collection<T> list,
                                           Function<T, K> keyFn,
                                           BinaryOperator<T> mergeFn) {
        return indexBy(list, keyFn, mergeFn, LinkedHashMap::new);
    }

    /**
     * 根据指定key函数将集合转为Map，key唯一，重复时用mergeFn处理，可指定Map类型。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param mergeFn 冲突时的合并函数
     * @param mapSupplier Map类型提供者
     * @return Map<K, T>
     */
    public static <T, K, M extends Map<K, T>> Map<K, T> indexBy(Collection<T> list,
                                                                Function<T, K> keyFn,
                                                                BinaryOperator<T> mergeFn,
                                                                java.util.function.Supplier<M> mapSupplier) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        keyFn,
                        Function.identity(),
                        mergeFn,
                        mapSupplier
                ));
    }

    /**
     * 根据key函数和value函数将集合转为Map，key唯一，重复时保留第一个。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param valueFn value提取函数
     * @return Map<K, V>
     */
    public static <T, K, V> Map<K, V> indexByValue(Collection<T> list,
                                                   Function<T, K> keyFn,
                                                   Function<T, V> valueFn) {
        return indexByValue(list, keyFn, valueFn, (a, b) -> a, LinkedHashMap::new);
    }

    /**
     * 根据key函数和value函数将集合转为Map，key唯一，重复时用mergeFn处理。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param valueFn value提取函数
     * @param mergeFn 冲突时的合并函数
     * @return Map<K, V>
     */
    public static <T, K, V> Map<K, V> indexByValue(Collection<T> list,
                                                   Function<T, K> keyFn,
                                                   Function<T, V> valueFn,
                                                   BinaryOperator<V> mergeFn) {
        return indexByValue(list, keyFn, valueFn, mergeFn, LinkedHashMap::new);
    }

    /**
     * 根据key函数和value函数将集合转为Map，key唯一，重复时用mergeFn处理，可指定Map类型。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param valueFn value提取函数
     * @param mergeFn 冲突时的合并函数
     * @param mapSupplier Map类型提供者
     * @return Map<K, V>
     */
    public static <T, K, V, M extends Map<K, V>> Map<K, V> indexByValue(Collection<T> list,
                                                                        Function<T, K> keyFn,
                                                                        Function<T, V> valueFn,
                                                                        BinaryOperator<V> mergeFn,
                                                                        java.util.function.Supplier<M> mapSupplier) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        keyFn,
                        valueFn,
                        mergeFn,
                        mapSupplier
                ));
    }

    /**
     * 根据key函数将集合分组，返回Map，key为分组字段，value为List。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @return Map<K, List<T>>
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> list, Function<T, K> keyFn) {
        return groupBy(list, keyFn, LinkedHashMap::new);
    }

    /**
     * 根据key函数将集合分组，返回Map，key为分组字段，value为List，可指定Map类型。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param mapSupplier Map类型提供者
     * @return Map<K, List<T>>
     */
    public static <T, K, M extends Map<K, List<T>>> Map<K, List<T>> groupBy(Collection<T> list,
                                                                            Function<T, K> keyFn,
                                                                            java.util.function.Supplier<M> mapSupplier) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        keyFn,
                        mapSupplier,
                        Collectors.toList()
                ));
    }

    /**
     * 根据key函数和value函数将集合分组，返回Map，key为分组字段，value为List。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param valueFn value提取函数
     * @return Map<K, List<V>>
     */
    public static <T, K, V> Map<K, List<V>> groupByValue(Collection<T> list,
                                                        Function<T, K> keyFn,
                                                        Function<T, V> valueFn) {
        return groupByValue(list, keyFn, valueFn, LinkedHashMap::new);
    }

    /**
     * 根据key函数和value函数将集合分组，返回Map，key为分组字段，value为List，可指定Map类型。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param valueFn value提取函数
     * @param mapSupplier Map类型提供者
     * @return Map<K, List<V>>
     */
    public static <T, K, V, M extends Map<K, List<V>>> Map<K, List<V>> groupByValue(Collection<T> list,
                                                                                    Function<T, K> keyFn,
                                                                                    Function<T, V> valueFn,
                                                                                    java.util.function.Supplier<M> mapSupplier) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        keyFn,
                        mapSupplier,
                        Collectors.mapping(valueFn, Collectors.toList())
                ));
    }

    /**
     * 根据key函数将集合分组，value为Set，去重。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @return Map<K, Set<T>>
     */
    public static <T, K> Map<K, Set<T>> multiMap(Collection<T> list, Function<T, K> keyFn) {
        return multiMap(list, keyFn, LinkedHashMap::new, LinkedHashSet::new);
    }

    /**
     * 根据key函数将集合分组，value为Set，去重，可指定Map和Set类型。
     * @param list 原始集合
     * @param keyFn key提取函数
     * @param mapSupplier Map类型提供者
     * @param setSupplier Set类型提供者
     * @return Map<K, Set<T>>
     */
    public static <T, K, M extends Map<K, Set<T>>> Map<K, Set<T>> multiMap(Collection<T> list,
                                                                           Function<T, K> keyFn,
                                                                           java.util.function.Supplier<M> mapSupplier,
                                                                           java.util.function.Supplier<Set<T>> setSupplier) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        keyFn,
                        mapSupplier,
                        Collectors.toCollection(setSupplier)
                ));
    }

    /**
     * 安全获取Map中的值，key不存在时返回null。
     * @param map Map对象
     * @param key 键
     * @return V
     */
    public static <K, V> V getOrNull(Map<K, V> map, K key) {
        return map == null ? null : map.get(key);
    }

    /**
     * 安全获取Map中的List值，key不存在时返回空List。
     * @param map Map对象
     * @param key 键
     * @return List<V>
     */
    public static <K, V> List<V> getOrEmpty(Map<K, List<V>> map, K key) {
        List<V> list = map == null ? null : map.get(key);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 安全获取Map中的Set值，key不存在时返回空Set。
     * @param map Map对象
     * @param key 键
     * @return Set<V>
     */
    public static <K, V> Set<V> getOrEmptySet(Map<K, Set<V>> map, K key) {
        Set<V> set = map == null ? null : map.get(key);
        return set == null ? Collections.emptySet() : set;
    }
}

