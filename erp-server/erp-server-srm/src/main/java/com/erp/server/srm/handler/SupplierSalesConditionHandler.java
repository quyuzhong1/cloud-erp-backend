package com.erp.server.srm.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SupplierSalesConditionHandler {

    public static String buildWhereClause(List<CfgSupplierSalesConditionEntity> conditionList,Boolean toUnderlineCase) {
        if (conditionList == null || conditionList.isEmpty()) {
            return "";
        }

        // 按 index 排序
        List<CfgSupplierSalesConditionEntity> sorted = conditionList.stream()
                .sorted(Comparator.comparing(CfgSupplierSalesConditionEntity::getIndex))
                .collect(Collectors.toList());

        StringBuilder sqlBuilder = new StringBuilder();
        boolean isFirstCondition = true;
        int i = 0; // 循环计数器
        int size = sorted.size(); // 条件数量

        for (CfgSupplierSalesConditionEntity cond : sorted) {
            String field = cond.getField();
            String compare = cond.getCompare();
            String value = cond.getValue();
            String valueType = cond.getValueType();
            String left = StringUtils.isNotBlank(cond.getLeftBracket()) ? cond.getLeftBracket() : "";
            String right = StringUtils.isNotBlank(cond.getRightBracket()) ? cond.getRightBracket() : "";
            String logic =  StringUtils.isNotBlank(cond.getLogic()) ? cond.getLogic() : " and ";

            String expression;
            if ("isPurchase".equals(field)) {
                // isPurchase 特殊处理
                expression = " 1 = 1 ";
            } else if ("categoryId".equals(field)) {
                // categoryId 特殊处理
                expression = buildCategoryIdCondition(compare, value, valueType);
            } else {
                // 普通字段
                //转下划线
                if(Boolean.TRUE.equals(toUnderlineCase)){
                    field = StrUtil.toUnderlineCase(field);
                }
                expression = buildSingleCondition(field, compare, value, valueType);
            }

            sqlBuilder.append(" ").append(left).append(expression).append(right).append(" ");

            if (i < size - 1) {
                sqlBuilder.append(" ").append(logic).append(" ");
            }
            i++;
        }

        return " ( "+sqlBuilder.toString().trim()+" ) and";
    }

    private static String buildCategoryIdCondition(String compare, String value, String valueType) {
        boolean isStringType = isQuoted(valueType);

        String sqlOp;
        String joiner; // 两个字段条件间逻辑符
        switch (compare) {
            case "==":
                sqlOp = "=";
                joiner = " OR ";
                break;
            case "!=":
                sqlOp = "!=";
                joiner = " AND ";
                break;
            case "inList":
                sqlOp = "IN";
                joiner = " OR ";
                break;
            case "notInList":
                sqlOp = "NOT IN";
                joiner = " AND ";
                break;
            default:
                throw new IllegalArgumentException("Unsupported compare for categoryId: " + compare);
        }

        String formatValue;
        if ("inList".equals(compare) || "notInList".equals(compare)) {
            formatValue = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .map(v -> isStringType ? ("'" + v + "'") : v)
                    .collect(Collectors.joining(", ", "(", ")"));
        } else {
            formatValue = isStringType ? ("'" + value.trim() + "'") : value.trim();
        }

        return "( first_category_id " + sqlOp + " " + formatValue
                + joiner +
                "second_category_id " + sqlOp + " " + formatValue +" )";
    }

    private static String buildSingleCondition(String field, String compare, String value, String valueType) {
        boolean isList = "inList".equalsIgnoreCase(compare) || "notInList".equalsIgnoreCase(compare);
        boolean isStringType = isQuoted(valueType);

        String formattedValue;
        if (isList) {
            formattedValue = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .map(v -> isStringType ? "'" + v + "'" : v)
                    .collect(Collectors.joining(", ", "(", ")"));
        } else {
            formattedValue = isStringType ? "'" + value.trim() + "'" : value.trim();
        }

        String sqlOp;
        switch (compare) {
            case "==":
                sqlOp = "=";
                break;
            case "!=":
                sqlOp = "!=";
                break;
            case "inList":
                sqlOp = "IN";
                break;
            case "notInList":
                sqlOp = "NOT IN";
                break;
            default:
                throw new IllegalArgumentException("Unsupported compare: " + compare);
        }

        return field + " " + sqlOp + " " + formattedValue;
    }

    private static boolean isQuoted(String valueType) {
        if (valueType == null) return false;
        return "String".equalsIgnoreCase(valueType)
                || "Date".equalsIgnoreCase(valueType)
                || "Timestamp".equalsIgnoreCase(valueType);
    }
}
