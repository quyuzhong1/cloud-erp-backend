package com.erp.server.oms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author jack
 * @date 2025-12-04
 */
@Component
public class KolB2cApplicationQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        if("true".equals(value)){
            return " kpi.disabled = true";
        }
        if("false".equals(value)){
            return " kpi.disabled = false";
        }
        return super.getSplicingSQL();
    }
}

