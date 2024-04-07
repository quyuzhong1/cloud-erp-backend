package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class TmsB2BDeclareQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("supplier")){
            return " exists (" +
                    "select 1 from logistics_bill lb where lb.outstock_id = db.source_id " +
                    "and lb.logistics_supplier_id " +compareCodeSplicingValueSql+
                    " )";
        }
        return null;
    }
}

