package com.erp.server.dmp.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class AdsErpInventoryDiffKingdeeQueryHandler extends AbstractQueryHandler {


	@Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
		if ("tab".equals(field)) {
            return super.getSplicingSQL();
        }
		return null;
    }

}
