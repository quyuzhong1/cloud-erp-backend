package com.erp.server.tms.query;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.common.business.query.AbstractQueryHandler;

@Component
public class TransferDeclareCostAllocationQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        
        return null;
    }

    /**
     * tabSql拼接
     */
    public String getTabSql(Object value) {
        if(!"all".equals(value)) {
        	super.buildDefaultDTO("g.report_status", Collections.singletonList(value));
        }
        
        return super.getSplicingSQL();
    }
}

