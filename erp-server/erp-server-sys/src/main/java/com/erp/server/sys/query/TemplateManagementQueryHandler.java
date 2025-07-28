package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class TemplateManagementQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if("size".equals(field)){
            return getSizeSql(value,compareCodeSplicingValueSql);
        }
        return null;
    }

    /**
     * @description: 纸张大小Sql拼接
     * @author jack
     * @date: 2025-07-24
     * @param value
     * @return String
     */
    private String getSizeSql(Object value, String compareCodeSplicingValueSql) {
        if (Objects.isNull(value) || value.equals("")) {
            return "";
        }
        String size = String.valueOf(value);
        String delimiter = null;

        if (size.contains("*")) {
            delimiter = "\\*";
        } else if (size.contains("x")) {
            delimiter = "x";
        } else {
            return "( tm.length " + compareCodeSplicingValueSql + " or tm.width " + compareCodeSplicingValueSql + " )";
        }

        String[] split = size.split(delimiter);
        StringBuilder sb = new StringBuilder();

        // 处理 length
        if (split.length > 0 && !StringUtils.isBlank(split[0])) {
            String trimmed = split[0].trim();
            String expr = compareCodeSplicingValueSql.replace(size, trimmed);
            sb.append("tm.length ").append(expr).append(" or ");
        }

        // 处理 width
        if (split.length > 1 && !StringUtils.isBlank(split[1])) {
            String trimmed = split[1].trim();
            String expr = compareCodeSplicingValueSql.replace(size, trimmed);
            sb.append("tm.width ").append(expr).append(" or ");
        }

        // 删除末尾多余的 " or "
        int length = sb.length();
        if (length >= 4 && " or ".equals(sb.substring(length - 4))) {
            sb.setLength(length - 4);
        }
        return "( " + sb.toString() + " )";
    }

    /**
     * @description: tabSql拼接
     * @author jack
     * @date: 2025-07-24
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }
        if ("false".equals(value)) {
            return "tm.disabled ="+ Boolean.TRUE;
        }
        if ("true".equals(value)) {
            return "tm.disabled ="+ Boolean.FALSE;
        }
        return "";
    }
}
