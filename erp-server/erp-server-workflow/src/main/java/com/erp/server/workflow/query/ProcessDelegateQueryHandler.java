package com.erp.server.workflow.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;


@Component
public class ProcessDelegateQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
