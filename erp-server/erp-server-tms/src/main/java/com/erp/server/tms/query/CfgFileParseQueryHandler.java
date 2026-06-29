package com.erp.server.tms.query;

import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 月结文件解析配置高级查询处理器。
 *
 * @author jack
 * @since 2026-06-29
 */
@Component
public class CfgFileParseQueryHandler extends AbstractQueryHandler {

    private static final Map<String, String> FIELD_COLUMN_MAP = new HashMap<>();

    static {
        FIELD_COLUMN_MAP.put("code", "cfp.code");
        FIELD_COLUMN_MAP.put("name", "cfp.name");
        FIELD_COLUMN_MAP.put("periodType", "cfp.period_type");
        FIELD_COLUMN_MAP.put("dictPlatform", "cfp.dict_platform");
        FIELD_COLUMN_MAP.put("dictPlatformName", "cfp.dict_platform_name");
        FIELD_COLUMN_MAP.put("folderType", "cfp.folder_type");
        FIELD_COLUMN_MAP.put("businessType", "cff.business_type");
        FIELD_COLUMN_MAP.put("type", "cff.type");
        FIELD_COLUMN_MAP.put("fileKeyword", "cff.file_keyword");
        FIELD_COLUMN_MAP.put("disabled", "cfp.disabled");
        FIELD_COLUMN_MAP.put("createUserName", "cfp.create_user_name");
        FIELD_COLUMN_MAP.put("createTime", "cfp.create_time");
        FIELD_COLUMN_MAP.put("updateUserName", "cfp.update_user_name");
        FIELD_COLUMN_MAP.put("updateTime", "cfp.update_time");
    }

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        String column = FIELD_COLUMN_MAP.get(field);
        if (column == null) {
            return null;
        }
        buildSplicingSQLDTO(column, AdvanceQueryContext.getCompareCode(), value, resolveDataType(field));
        return getSplicingSQL();
    }

    /**
     * 处理列表页签的启用、停用过滤。
     *
     * @param value tab 值，all=全部，enabled=启用，disabled=停用
     * @return tab 对应的 SQL 条件
     */
    private String getTabSql(Object value) {
        if ("all".equals(value) || "".equals(value)) {
            return getQueryAllSql();
        }
        if ("enabled".equals(value)) {
            return " cfp.disabled = false ";
        }
        if ("disabled".equals(value)) {
            return " cfp.disabled = true ";
        }
        return null;
    }

    /**
     * 根据查询字段选择数据类型。
     *
     * @param field 前端字段名
     * @return 查询数据类型
     */
    private QueryDataTypeEnum resolveDataType(String field) {
        if ("disabled".equals(field)) {
            return QueryDataTypeEnum.BOOLEAN;
        }
        if ("createTime".equals(field) || "updateTime".equals(field)) {
            return QueryDataTypeEnum.DATE;
        }
        return QueryDataTypeEnum.STRING;
    }
}
