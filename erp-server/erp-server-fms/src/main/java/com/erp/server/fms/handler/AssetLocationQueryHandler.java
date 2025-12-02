package com.erp.server.fms.handler;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


@Component
public class AssetLocationQueryHandler  extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        super.buildDefaultDTO("al.approve_status", value);
        return super.getSplicingSQL();
    }
}
