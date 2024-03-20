package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author Lambda
 * @Classname DictParentBaseQueryHandler
 * @Description TODO
 * @Date 2024-03-19 16:54
 * @Created by yl
 */
@Component
public class DictParentBaseQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
