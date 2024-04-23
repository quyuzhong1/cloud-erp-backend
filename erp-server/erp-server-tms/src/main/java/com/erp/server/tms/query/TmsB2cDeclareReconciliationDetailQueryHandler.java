package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class TmsB2cDeclareReconciliationDetailQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //查询待对账明细
        if("waitReconciliationDetail".equals(field)){
            super.buildDefaultDTO("tbdrd.main_id","");
            super.buildDefaultDTO("tbdrd.logistics_supplier_id",value);
        }
        return null;
    }
}

