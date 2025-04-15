package com.common.core.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @Classname EntityUpdateHelper
 * @Description 实体更新助手类
 * @Date 2025-03-19
 * @Created jack
 */
public class EntityUpdateHelper {

    private EntityUpdateHelper() {
    }
    /**
     * 默认动态更新非空或者大于0的字段
     * @param entity 需要更新的实体对象
     * @param <T> 实体类型
     * @return 返回构造的 UpdateWrapper 对象
     */
    public static <T extends BaseEntity> UpdateWrapper<T> createUpdateWrapper(T entity) {
        return createUpdateWrapper(entity, field -> true);
    }



    /**
     * 创建一个更新封装对象，用于实体类的更新操作
     * 该方法根据给定的实体和字段筛选条件，动态生成更新SQL语句
     * @param entity 需要更新的实体对象
     * @param fieldPredicate 自定义的字段筛选条件，决定哪些字段需要被更新
     * @param <T> 实体类型
     * @return 返回构造的 UpdateWrapper 对象
     */
    public static <T extends BaseEntity> UpdateWrapper<T> createUpdateWrapper(T entity, Predicate<Field> fieldPredicate) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (shouldSkipField(field)) continue;
            field.setAccessible(true);
            // 如果字段满足自定义的 Predicate 条件，则执行更新
            if (fieldPredicate.test(field)) {
                try {
                    Object value = field.get(entity);
                    if (shouldUpdate(value)) { // 判断该字段是否符合更新条件
                        String columnName = getColumnName(field);
                        // 如果是 ID 字段，使用 eq 条件
                        if ("id".equalsIgnoreCase(columnName)) {
                            updateWrapper.eq(columnName, value);
                        } else {
                            updateWrapper.set(columnName, value);
                        }
                    }
                } catch (Exception e) {
                    throw new ServiceException(ApiError.ERROR_CREATE_UPDATE_WRAPPER_ERROR,e.getMessage());
                }
            }
        }

        return updateWrapper;
    }

    /**
     * 创建一个更新封装对象，自动排除指定字段
     *
     * @param entity 需要更新的实体对象
     * @param excludeFields 需要排除更新的字段集合
     * @param <T> 实体类型
     * @return 返回构造的 UpdateWrapper 对象
     */
    public static <T extends BaseEntity> UpdateWrapper<T> createUpdateWrapper(T entity, Set<String> excludeFields) {
        return createUpdateWrapper(entity, field -> !excludeFields.contains(field.getName()));
    }

    /**
     * 判断是否应该跳过给定的字段
     * 本方法通过检查字段的修饰符来决定是否跳过该字段
     * 主要跳过那些被声明为 static 或 final 的字段，因为这些字段不参与某些特定的操作或序列化过程
     *
     * @param field 待检查的字段对象
     * @return 如果字段应被跳过，则返回true；否则返回false
     */
    private static boolean shouldSkipField(Field field) {
        int modifiers = field.getModifiers();
        // 跳过 static 和 final 修饰的字段
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
            return true;
        }
        return false;
    }

    /**
     * 根据 Object 类型判断是否应该更新
     *
     * @param value 字段的值
     * @return 是否需要更新
     */
    private static boolean shouldUpdate(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            // 非空字符串才更新
            return !((String) value).trim().isEmpty();
        } else if (value instanceof Number) {
            // 数值类型大于0才更新
            return ((Number) value).doubleValue() > 0;
        }else {
            // 其他类型默认不为 null 就更新
            return true;
        }
    }

    /**
     * 获取字段对应的数据库列名
     *
     * @param field 字段对象
     * @return 数据库列名
     */
    private static String getColumnName(Field field) {
        // 先检查是否有 @TableField 注解
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && !tableField.value().isEmpty()) {
            return tableField.value(); // 返回 @TableField 指定的数据库列名
        }
        // 否则执行驼峰转下划线转换
        return camelToSnake(field.getName());
    }

    /**
     * 将驼峰命名转换为下划线
     */
    private static String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
