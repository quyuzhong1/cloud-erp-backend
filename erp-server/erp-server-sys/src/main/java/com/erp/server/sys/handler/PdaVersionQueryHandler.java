package com.erp.server.sys.handler;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @Author: wtr
 * @Date: 2026/4/22 10:22
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class PdaVersionQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return this.getQueryAllSql();
    }
}