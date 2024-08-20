package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 中转报关表
 */
@Component
public class FirstMileCostAllocationQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        if (Objects.equals("", value) || Objects.equals("all", value)){
            return this.getQueryAllSql();
        }else {
            this.buildDefaultDTO("a.status", value);
        }
        return super.getSplicingSQL();
    }
}

