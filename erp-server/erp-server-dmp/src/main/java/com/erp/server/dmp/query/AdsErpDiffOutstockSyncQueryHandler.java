package com.erp.server.dmp.query;

import org.springframework.stereotype.Component;

import com.common.business.query.AbstractQueryHandler;

@Component
public class AdsErpDiffOutstockSyncQueryHandler extends AbstractQueryHandler {


	@Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
		if ("tab".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }else {
            	return " t.diff_tag = '"+ searchType +"' ";
            }
        }
		return null;
    }

}
