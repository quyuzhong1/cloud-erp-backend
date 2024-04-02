package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class TmsFirstMileReconciliationDetailQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}

