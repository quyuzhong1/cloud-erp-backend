package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class TmsFmDeclareQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("supplier")){
            return " exists (" +
                    "select 1 from logistics_bill lb where lb.outstock_id = db.source_id " +
                    "and lb.supplier_id " +compareCodeSplicingValueSql+
                    " )";
        }

        if(field.equals("counterCode")){
            return " exists (" +
                    "select 1 from logistics_bill lb where lb.outstock_id = db.source_id " +
                    "and lb.counter_no " +compareCodeSplicingValueSql+
                    " )";
        }

        return null;
    }
}

