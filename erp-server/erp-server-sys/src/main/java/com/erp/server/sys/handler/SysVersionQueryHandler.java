package com.erp.server.sys.handler;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @Author: wtr
 * @Date: 2026/4/10 9:36
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class SysVersionQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return this.getQueryAllSql();
    }
}
