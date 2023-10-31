package com.baomidou.mybatisplus.syslog;

import cn.hutool.core.util.ReflectUtil;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Slf4j
public class JavadocReader {

    public static void main(String[] args) throws Exception {
        List<Class<?>> list = Arrays.asList(
                BiTargetNewProductSettingDTO.ViewDTO.class
//                SoInfoDTO.ViewDTO.class,
//                ProjectPlanDetailsVO.class,
//                SubjectLayoutDetailsDTO.class
        );
        for (Class<?> clazz : list) {
            // 是否是内部类
            Map<String, String> map = readToJavadocMap(clazz);
            System.out.println(map);
        }

    }

    /**
     * 指定calss生成Map<字段名，javadoc信息>
     */
    public static Map<String, String> readToJavadocMap(Class<?> clazz) throws Exception {
        // 解析成Java路径
        String javaPath = parseJavaPath(clazz);
        log.info("java class路径={}", javaPath);
        return readJavadocWithSuperClass("", javaPath, clazz);

    }

    /**
     * 读取指定class的javadoc并包括超类
     */
    private static Map<String, String> readJavadocWithSuperClass(String detailKeyName, String javaPath, Class<?> clazz) throws Exception {
        Map<String, String> map = readJavadoc(detailKeyName, javaPath, clazz);
        Class<?> superclass = clazz.getSuperclass();
        if (null == superclass) {
            return map;
        }
        if ("com.common.core.entity.BaseEntity".equalsIgnoreCase(superclass.getName())){
            return map;
        }
        if (" com.baomidou.mybatisplus.extension.activerecord.Model".equalsIgnoreCase(superclass.getName())){
            return map;
        }
        if ("java.lang.Object".equalsIgnoreCase(superclass.getName())) {
            return map;
        }
        String superJavaPath = parseJavaPath(superclass);
        Map<String, String> superMap = readJavadocWithSuperClass(detailKeyName, superJavaPath, superclass);
        map.putAll(superMap);
        return map;
    }


    /**
     * 解析详情
     */
    private static Map<String, String> parseDetailMap(Field field) throws Exception {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Class<?> detailClass = (Class<?>) genericType.getActualTypeArguments()[0];
        String detailJavaClassPath = parseJavaPath(detailClass);
        return readJavadocWithSuperClass(field.getName().concat("."), detailJavaClassPath, detailClass);
    }

    /**
     * class解析成java路径
     */
    private static String parseJavaPath(Class<?> clazz) {
        // 执行类路径
        String classPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (classPath.startsWith("/")) {
            classPath = classPath.substring(1);
        }
        // F:/Project/weiji/cloud-erp/erp-model/erp-model-bi/target/classes/
        System.out.println(("当前执行的类路径：classPath=" + classPath));
        String startStr = classPath.replace("target/classes", "src/main/java");
        String className = clazz.getName();
        System.out.println(("当前执行的类名：className=" + className));
        String endStr = className.replace(".", "/").replaceAll("\\$.*", "").concat(".java");
        // F:\Project\weiji\cloud-erp\erp-model\erp-model-bi\src\main\java\com\erp\model\bi\dto\BiTargetNewProductSettingDTO.java
        String allPath = startStr.concat(endStr);
        return allPath.replace("/", "\\");
    }

    /**
     * 读取javadoc信息,如果有子类key为（超类字段名.子类字段名），只支持2层
     */
    public static Map<String, String> readJavadoc(String detailKeyName, String sourceFilePath, Class<?> clazz) throws Exception {
        Map<String, String> map = new HashMap<>();
        // 内容类Class名称
        String classSimpleName = clazz.getSimpleName();

        // 读取java文件
        FileInputStream fileInputStream = new FileInputStream(sourceFilePath);
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(fileInputStream);
        if (!parseResult.isSuccessful()) {
            System.out.println(("Failed to parse the source file."));
        }

        CompilationUnit compilationUnit = parseResult.getResult().get();
        List<ClassOrInterfaceDeclaration> declarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration declaration : declarationList) {
            System.out.println("当前：" + declaration.getName());
            // 内部类
            if (declaration.getName().toString().equalsIgnoreCase(classSimpleName)) {
                String targetClassName = declaration.getNameAsString();
                System.out.println("指定类" + targetClassName);

                Field[] fields = ReflectUtil.getFields(clazz);
                for (Field field : fields) {
                    String fieldName = field.getName();
                    if (List.class.isAssignableFrom(field.getType()) && (fieldName.contains("detail") || fieldName.contains("Detail"))) {
                        // 解析详情
                        Map<String, String> details = parseDetailMap(field);
                        map.putAll(details);
                        continue;
                    }
                    FieldDeclaration fieldDeclaration = declaration
                            .getFieldByName(fieldName)
                            .orElse(null);
                    if (null == fieldDeclaration) {
                        System.out.println("No fieldDeclaration found for field: " + fieldName);
                        continue;
                    }
                    Optional<Javadoc> javadocComment = fieldDeclaration.getJavadoc();
                    if (javadocComment.isPresent()) {
                        JavadocDescription description = javadocComment.get().getDescription();
                        System.out.println("code=" + fieldName + ": " + description.toText().replace("\n", ""));
                        map.put(detailKeyName.concat(fieldName), description.toText());
                    } else {
                        System.out.println("No Javadoc found for field: " + fieldName);
                    }
                }
            }
        }
        return map;
    }
}
