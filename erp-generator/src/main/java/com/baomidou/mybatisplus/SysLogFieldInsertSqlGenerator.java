package com.baomidou.mybatisplus;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.syslog.JavadocReader;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.model.bi.entity.*;
import com.erp.model.bi.entity.BiSysModuleEntity;
import com.erp.model.dmp.dto.*;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.vo.ProjectPlanDetailsVO;
import com.erp.model.plm.vo.SysTaskVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysLogRecordFieldEntity;
import com.erp.model.sys.entity.SysRoleEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 指定class生成sys_log_save_field表的插入sql
 * 自动生成工具
 */
public class SysLogFieldInsertSqlGenerator {


    /**
     * 指定需要生成配置的class类(可修改)
     */
    public final static List<Class<?>> targetClassList;

    static {
        targetClassList = Arrays.asList(
                BiSysModuleEntity.class,
                BiDictEntity.class,
                SubjectLayoutDetailsDTO.class,
                ModuleDTO.class,
                DmpShopInfoDTO.class,
                BasicLabelEntity.class,
                BomDTO.class,
                NoticeMessageEntity.class,
                ProductDTO.class,
                ProductPlanDetailsDTO.class,
                ProjectPlanDetailsVO.class,
                ProjectTaskVO.class,
                SysTaskVO.class,
                DocsDTO.class,
                SysAccountingCompanyEntity.class,
                SysRoleEntity.class,
                SysUserInfoEntity.class
                );
    }

    /**
     * 指定需要生成配置的class类(可修改)
     */
    public final static Class<?> targetClass = BiTargetNewProductSettingDTO.ViewDTO.class;

    /**
     * 主类忽略记录的字段名(可修改)
     */
    public final static String[] targetDtoExcludesFields = new String[]{
            "id",
            "code"
    };

    /**
     * 明细忽略记录的字段名(可修改)
     */
    public final static String[] targetDetailDtoExcludesFields = new String[]{
            "id",
            "mainId"
    };

    /**
     * 项目路径
     */
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    /**
     * 插入的日志配置表
     */
    public final static String sysLogFieldTableName = "\"erp-sys\".\"public\".\"sys_log_record_field\"";

    /**
     * sql模板
     */
    public final static String insertSqlTemplate = "insert into {} ({}) values ({});";

    /**
     * 配置记录忽略添加的字段
     */
    public final static String[] sysLogExcludesFields = new String[]{"id",
            "createTime",
            "updateTime",
            "createUserName",
            "createUserId",
            "updateUserName",
            "updateUserId",
            "version",
            "isDeleted"
    };

    public static void main(String[] args) throws Exception {
        List<Map<String, String>> params;
        if (!CollectionUtils.isEmpty(targetClassList)) {
            params = createParamsMapByList(targetClassList, targetDtoExcludesFields, targetDetailDtoExcludesFields);
        } else if (null != targetClass) {
            params = createParamsMap(targetClass, targetDtoExcludesFields, targetDetailDtoExcludesFields);
        } else {
            throw new RuntimeException("请输入targetClassList或targetClass");
        }
        System.out.println("==========================生成插入sql开始...==========================");
        for (Map<String, String> param : params) {
            String insertSql = genInsertSql(sysLogFieldTableName, SysLogRecordFieldEntity.class, new HashMap<>(), sysLogExcludesFields, param);
            System.out.println(insertSql);
        }
        System.out.println("==========================生成插入sql结束！！！==========================");

    }

    private static List<Map<String, String>> createParamsMapByList(List<Class<?>> targetClassList, String[] targetDtoExcludesFields, String[] targetDetailDtoExcludesFields) throws Exception{
        List<Map<String, String>> params = new LinkedList<>();
        for (Class<?> aClass : targetClassList) {
            List<Map<String, String>> paramsMap = createParamsMap(aClass, targetDtoExcludesFields, targetDetailDtoExcludesFields);
            params.addAll(paramsMap);
        }
        return params;
    }


    private static List<Map<String, String>> createParamsMap(Class<?> aClass, String[] targetDtoExcludesFields, String[] targetDetailDtoExcludesFields) throws Exception {
        Map<String, String> javadocMap = JavadocReader.readToJavadocMap(aClass);
        List<Map<String, String>> result = new LinkedList<>();
        for (Field field : ReflectUtil.getFields(aClass)) {
            if (Arrays.stream(targetDtoExcludesFields).anyMatch(e -> e.contains(field.getName()))) {
                continue;
            }
            if (field.getType() == List.class) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    Class<?> detailClazz = (Class<?>) pt.getActualTypeArguments()[0];
                    for (Field fieldDetail : ReflectUtil.getFields(detailClazz)) {
                        if (Arrays.stream(targetDetailDtoExcludesFields).anyMatch(e -> e.contains(fieldDetail.getName()))) {
                            continue;
                        }
                        Map<String, String> map = putDetailToMap(field, aClass, fieldDetail, javadocMap);
                        result.add(map);
                    }
                }
            } else {
                Map<String, String> map = createMap(aClass, field, javadocMap);
                result.add(map);
            }
        }
        return result;
    }

    private static Map<String, String> putDetailToMap(Field field, Class<?> aClass, Field fieldDetail, Map<String, String> javadocMap) {
        Map<String, String> params = new HashMap<>();
        String detailName = field.getName().concat(".").concat(fieldDetail.getName());
        params.put("field", detailName);
        String javadocField = javadocMap.getOrDefault(detailName, "");
        params.put("field_name", javadocField);
        params.put("class_path", String.valueOf(aClass));
        Class<?> fieldType = field.getType();
        if (fieldType.isEnum()) {
            params.put("type", "2");
            params.put("enum_class", String.valueOf(fieldType));
        } else if (fieldType.isAssignableFrom(Boolean.class)) {
            params.put("type", "1");
        } else if (fieldType.isAssignableFrom(LocalDateTime.class) || fieldType.isAssignableFrom(LocalDate.class)) {
            params.put("type", "0");
        } else {
            params.put("type", "0");
        }
        return params;
    }

    private static Map<String, String> createMap(Class<?> aClass, Field field, Map<String, String> javadocMap) {
        Map<String, String> params = new HashMap<>();
        params.put("field", field.getName());
        String javadocField = javadocMap.getOrDefault(field.getName(), "");
        params.put("field_name", javadocField);
        params.put("class_path", String.valueOf(aClass));
        Class<?> fieldType = field.getType();
        if (fieldType.isEnum()) {
            params.put("type", "2");
            params.put("enum_class", String.valueOf(fieldType));
        } else if (fieldType.isAssignableFrom(Boolean.class)) {
            params.put("type", "1");
        } else if (fieldType.isAssignableFrom(LocalDateTime.class) || fieldType.isAssignableFrom(LocalDate.class)) {
            params.put("type", "0");
        } else {
            params.put("type", "0");
        }
        return params;
    }


    /**
     * <p>功能: 生成插入的SQL</p>
     *
     * @param tableName  表名
     * @param aClass     表对应的model运行时类 <br>
     * @param replaceMap 需要替换的字段map <br>
     *                   <i>注: 有些字段模型里的名字和数据库里的名字不一致，需要用这个字段指定所需要的名字</i>
     * @param excludes   需要排除的字段 <br>
     *                   <i>注: 有些字段模型里有但是数据库里不需要这个字段，需要用这个字段指定要排除的字段名</i>
     * @return 生成的SQL
     */
    public static String genInsertSql(String tableName,
                                      Class<?> aClass,
                                      Map<String, String> replaceMap,
                                      String[] excludes,
                                      Map<String, String> valueMap
    ) {
        //获取参数
        List<String> params = getParams(aClass, replaceMap, excludes);
        Map<String, String> resultMap = new TreeMap<>();
        resultMap.put("\"id\"", IdWorker.getIdStr());
        for (String param : params) {
            String value = valueMap.getOrDefault(param, "");
            if (StringUtils.isBlank(value)) {
                value = "''";
            } else {
                value = StrUtil.format("'{}'", value);
            }
            String sqlFieldName = StrUtil.format("\"{}\"", param);
            resultMap.put(sqlFieldName, value);
        }
        String insertNameList = String.join(",", resultMap.keySet());
        String insertValueList = String.join(",", resultMap.values());
        return StrUtil.format(insertSqlTemplate, sysLogFieldTableName, insertNameList, insertValueList);
    }

    /**
     * <p>功能: 反射获取model中的所有参数名</p>
     *
     * @param clz        模型的运行时类
     * @param replaceMap 指定需要替换的参数名
     * @param excludes   需要排除的字段
     */
    private static List<String> getParams(Class<?> clz, Map<String, String> replaceMap, String[] excludes) {
        List<String> params = Lists.newArrayList();

        //获取所有参数
        Field[] fields = ReflectUtil.getFields(clz);
        for (Field field : fields) {
            TableField annotation = field.getAnnotation(TableField.class);
            if (null == annotation) {
                continue;
            }
            String name = field.getName();
            //排除excludes中的字段
            if (Arrays.stream(excludes).anyMatch(e -> e.contains(field.getName()))) {
                continue;
            }
            //若map中有替换的参数名，则替换
            if (replaceMap != null && replaceMap.get(name) != null) {
                params.add(replaceMap.get(name));
            } else {
                params.add(field.getName());
            }
        }
        //按照首字母排序
        Collections.sort(params);
        //把大写字母前加'_'，然后大写转小写
        params = toTableFieldNames(params);
        return params;
    }

    /**
     * <p>功能: 将model的参数名转换为对应table的参数名，也就是把大写字母前加'_'，然后大写转小写</p>
     */
    private static List<String> toTableFieldNames(List<String> ModelNames) {
        List<String> tableNames = Lists.newArrayList();
        for (String name : ModelNames) {
            for (char c : name.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    name = name.replace(c + "", "_" + Character.toLowerCase(c));
                }
            }
            tableNames.add(name);
        }
        return tableNames;
    }

    /**
     * <p>功能: 替换掉SQL中的第一个逗号','</p>
     */
    private static void replaceComma(StringBuilder sb) {
        //替换掉参数名中的第一个逗号','
        int i = sb.indexOf(",");
        sb.replace(i, i + 1, "");
        //替换掉占位符'?'的第一个逗号','
        int j = sb.indexOf(",?");
        sb.replace(j, j + 1, "");
    }


}