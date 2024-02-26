package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MachineInfoQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)) {
            if (StringUtils.isBlank(value.toString())) {
                return getQueryAllSql();
            }
        }
        return null;
    }
}
