package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author Lambda
 * @Classname KingdeeUserQueryHandler
 * @Description TODO
 * @Date 2024-03-14 10:45
 * @Created by yl
 */
@Component
public class KingdeeUserQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
