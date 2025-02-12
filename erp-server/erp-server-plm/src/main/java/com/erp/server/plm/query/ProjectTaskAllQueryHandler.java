package com.erp.server.plm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class ProjectTaskAllQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("pt.priority".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pt.priority is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pt.priority is not null";
            }
            return " pt.priority " + compareCodeSplicingValueSql;
        }

        if ("pt.status".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pt.status is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pt.status is not null";
            }
            return " pt.status " + compareCodeSplicingValueSql;
        }
        return null;
    }
}
