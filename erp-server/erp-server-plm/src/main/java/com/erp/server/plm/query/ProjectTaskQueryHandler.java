package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 项目任务高级查询
 * @date 2024-08-29
 * @author tanmujin
 */
@Component
public class ProjectTaskQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return "";
    }
}
