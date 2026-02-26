package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异步任务记录查询处理器
 *
 * @author YourName
 * @date 2026/02/26
 */
@Component
public class TmsAsyncTaskRecordQueryHandler extends AbstractQueryHandler {

    @Override
    public String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql)  {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return "";
    }

    private String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        return "";
    }
}