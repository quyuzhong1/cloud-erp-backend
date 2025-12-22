package com.erp.server.oms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @Author: wtr
 * @Date: 2025/12/1 10:25
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class DeliveryBoxRuleQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return "1 = 1";
    }
}
