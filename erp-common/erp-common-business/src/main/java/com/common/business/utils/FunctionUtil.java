package com.common.business.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Java8通过Function函数获取字段名称(获取实体类的字段名称)
 */
@Slf4j
public class FunctionUtil {

    /**
     * 字段名注解,声明表字段
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TableField {
        String value() default "";
    }

    //默认配置
    static String defaultSplit = "";
    static Integer defaultToType = 0;

    /**
     * 获取实体类的字段名称(实体声明的字段名称)
     */
    public static <T> String getFieldName(SFunction<T, ?> fn) {
        return getFieldName(fn, defaultSplit);
    }

    /**
     * 获取实体类的字段名称
     * @param split 分隔符，多个字母自定义分隔符
     */
    public static <T> String getFieldName(SFunction<T, ?> fn, String split) {
        return getFieldName(fn, split, defaultToType);
    }

    /**
     * 获取实体类的字段名称
     * @param split 分隔符，多个字母自定义分隔符
     * @param toType 转换方式，多个字母以大小写方式返回 0.不做转换 1.大写 2.小写
     */
    public static <T> String getFieldName(SFunction<T, ?> fn, String split, Integer toType) {
        SerializedLambda serializedLambda = getSerializedLambda(fn);

        // 从lambda信息取出method、field、class等
        String fieldName = serializedLambda.getImplMethodName().substring("get".length());
        fieldName = fieldName.replaceFirst(fieldName.charAt(0) + "", (fieldName.charAt(0) + "").toLowerCase());
        Field field;
        try {
            field = Class.forName(serializedLambda.getImplClass().replace("/", ".")).getDeclaredField(fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        // 从field取出字段名，可以根据实际情况调整
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && tableField.value().length() > 0) {
            return tableField.value();
        } else {

            //0.不做转换 1.大写 2.小写
            switch (toType) {
                case 1:
                    return fieldName.replaceAll("[A-Z]", split + "$0").toUpperCase();
                case 2:
                    return fieldName.replaceAll("[A-Z]", split + "$0").toLowerCase();
                default:
                    return fieldName.replaceAll("[A-Z]", split + "$0");
            }

        }

    }

    private static <T> SerializedLambda getSerializedLambda(SFunction<T, ?> fn) {
        // 从function取出序列化方法
        Method writeReplaceMethod;
        try {
            writeReplaceMethod = fn.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // 从序列化方法取出序列化的lambda信息
        boolean isAccessible = writeReplaceMethod.isAccessible();
        writeReplaceMethod.setAccessible(true);
        SerializedLambda serializedLambda;
        try {
            serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(fn);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        writeReplaceMethod.setAccessible(isAccessible);
        return serializedLambda;
    }


    /**
     * 测试用户实体类
     */
    public static class TestUserDemo implements Serializable {

        private static final long serialVersionUID = 1L;

        private String loginName;
        private String name;
        private String companySimpleName;

        private String nickName;

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCompanySimpleName() {
            return companySimpleName;
        }

        public void setCompanySimpleName(String companySimpleName) {
            this.companySimpleName = companySimpleName;
        }
    }


    /**
     * 参考示例
     */
    public static void main(String[] args) {

        //实体类原字段名称返回
        log.info("实体类原字段名称返回");
        log.info("字段名：" + getFieldName(TestUserDemo::getName));
        log.info("字段名：" + getFieldName(TestUserDemo::getNickName));
        log.info("字段名：" + getFieldName(TestUserDemo::getCompanySimpleName));

        log.info("实体类字段名称增加分隔符");
        log.info("字段名：" + getFieldName(TestUserDemo::getCompanySimpleName, "_"));

        log.info("实体类字段名称增加分隔符 + 大小写");
        log.info("字段名：" + getFieldName(TestUserDemo::getCompanySimpleName, "_", 0));
        log.info("字段名：" + getFieldName(TestUserDemo::getCompanySimpleName, "_", 1));
        log.info("字段名：" + getFieldName(TestUserDemo::getCompanySimpleName, "_", 2));


    }


}

