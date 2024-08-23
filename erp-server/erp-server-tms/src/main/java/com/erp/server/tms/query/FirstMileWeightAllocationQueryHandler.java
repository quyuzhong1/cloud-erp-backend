package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 头程重量分摊
 * @date 2024-08-22
 * @author tanmujin
 */
@Component
public class FirstMileWeightAllocationQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return "";
    }
}
