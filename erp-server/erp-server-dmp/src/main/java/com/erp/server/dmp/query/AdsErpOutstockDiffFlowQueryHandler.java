package com.erp.server.dmp.query;

import org.springframework.stereotype.Component;

import com.common.business.query.AbstractQueryHandler;

@Component
public class AdsErpOutstockDiffFlowQueryHandler extends AbstractQueryHandler {


	@Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }

}
