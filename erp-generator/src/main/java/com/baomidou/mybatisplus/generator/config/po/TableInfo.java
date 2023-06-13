/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baomidou.mybatisplus.generator.config.po;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 表信息，关联到当前字段信息
 *
 * @author YangHu
 * @since 2016/8/30
 */
@Data
@Accessors(chain = true)
public class TableInfo {

    private final Set<String> importPackages = new HashSet<>();
    private boolean convert;
    private String name;
    private String upperName;
    private String comment;
    private String entityName;
    private String mapperName;
    private String xmlName;
    private String serviceName;
    private String serviceImplName;
    private String controllerName;
    private List<TableField> fields;
    private String dtoName;
    private String voName;
    /**
     * 公共字段
     */
    private List<TableField> commonFields;
    private List<String> commonFieldNames;
    private String fieldNames;
    private List<String> fieldNameSet;
    private String includeAllBaseEntityField;

    public TableInfo setConvert(boolean convert) {
        this.convert = convert;
        return this;
    }

    protected TableInfo setConvert(StrategyConfig strategyConfig) {
        if (strategyConfig.containsTablePrefix(name)) {
            // 包含前缀
            this.convert = true;
        } else if (strategyConfig.getEntityTableFieldAnnotationEnable()) {
            this.convert = true;
        }else if (strategyConfig.isCapitalModeNaming(name)) {
            // 包含
            this.convert = false;
        } else {
            // 转换字段
            if (NamingStrategy.underline_to_camel == strategyConfig.getColumnNaming()) {
                // 包含大写处理
                if (StringUtils.containsUpperCase(name)) {
                    this.convert = true;
                }
            } else if (!entityName.equalsIgnoreCase(name)) {
                this.convert = true;
            }
        }
        return this;
    }

    public String getEntityPath() {
        // 主要用于控制器路径
        String entityPath = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
        // 如果是以Entity结尾，则去掉
        if(entityPath.endsWith(ConstVal.ENTITY)) {
            entityPath = entityPath.substring(0, entityPath.length() - ConstVal.ENTITY.length());
        }
        return entityPath;
    }

    public TableInfo setEntityName(StrategyConfig strategyConfig, String entityName) {
        this.entityName = entityName;
        this.setConvert(strategyConfig);
        return this;
    }

    public TableInfo setFields(List<TableField> fields) {
        if (CollectionUtils.isNotEmpty(fields)) {
            this.fields = fields;
            // 收集导入包信息
            for (TableField field : fields) {
                if (null != field.getColumnType() && null != field.getColumnType().getPkg()) {
                    importPackages.add(field.getColumnType().getPkg());
                }
                if (field.isKeyFlag()) {
                    // 主键
                    if (field.isConvert() || field.isKeyIdentityFlag()) {
                        importPackages.add(com.baomidou.mybatisplus.annotation.TableId.class.getCanonicalName());
                    }
                    // 自增
                    if (field.isKeyIdentityFlag()) {
                        importPackages.add(com.baomidou.mybatisplus.annotation.IdType.class.getCanonicalName());
                    }
                } else if (field.isConvert()) {
                    // 普通字段
                    importPackages.add(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
                }
                if (null != field.getFill()) {
                    // 填充字段
                    importPackages.add(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
                    importPackages.add(com.baomidou.mybatisplus.annotation.FieldFill.class.getCanonicalName());
                }
            }
        }
        return this;
    }

    public TableInfo setImportPackages(String pkg) {
        importPackages.add(pkg);
        return this;
    }

    public TableInfo removeImportPackages(String pkg) {
        importPackages.remove(pkg);
        return this;
    }

    /**
     * 逻辑删除
     */
    public boolean isLogicDelete(String logicDeletePropertyName) {
        return fields.parallelStream().anyMatch(tf -> tf.getName().equals(logicDeletePropertyName));
    }

    /**
     * 转换filed实体为 xml mapper 中的 base column 字符串信息
     */
    public String getFieldNames() {
        if (StringUtils.isEmpty(fieldNames)) {
            StringBuilder names = new StringBuilder();
            IntStream.range(0, fields.size()).forEach(i -> {
                TableField fd = fields.get(i);
                if (i == fields.size() - 1) {
                    names.append(fd.getName());
                } else {
                    names.append(fd.getName()).append(", ");
                }
            });
            fieldNames = names.toString();
        }
        return fieldNames;
    }

    public List<String> getCommonFieldNames() {
        return commonFields.stream().map(TableField::getName).collect(Collectors.toList());
    }

    public List<String> getFieldNameSet() {
        return fields.stream().map(TableField::getName).collect(Collectors.toList());
    }

    public String getIncludeAllBaseEntityField() {
        return includeAllBaseEntityField;
    }

    public void setIncludeAllBaseEntityField(String includeAllBaseEntityField) {
        this.includeAllBaseEntityField = includeAllBaseEntityField;
    }

    public String getDtoName() {
        if(dtoName.endsWith(ConstVal.DTO)) { // 把后面2位转换成大写
            dtoName = dtoName.substring(0, dtoName.length() - ConstVal.DTO.length()) + ConstVal.DTO.toUpperCase();
        }
        return dtoName;
    }

}
