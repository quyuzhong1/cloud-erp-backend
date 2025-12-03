package com.erp.server.oms.query;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import org.springframework.stereotype.Component;

/**
 * @author jack
 * @date 2025-12-03
 */
@Component
public class KolPartnerInfoQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        return super.getSplicingSQL();
    }
}

