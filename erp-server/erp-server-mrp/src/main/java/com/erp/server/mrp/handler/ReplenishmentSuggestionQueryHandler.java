package com.erp.server.mrp.handler;


import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class ReplenishmentSuggestionQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("label".equals(field)) {

        }

        return null;
    }
}
