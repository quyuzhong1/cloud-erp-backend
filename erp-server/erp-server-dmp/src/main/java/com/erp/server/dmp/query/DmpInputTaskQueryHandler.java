package com.erp.server.dmp.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Component
public class DmpInputTaskQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if ("all".equals(value)) {
                return getQueryAllSql();
            }
            if ("waitFinish".equals(value)) {
                List<String> waitFinishStatus = Arrays.asList("init", "fds", "mongo", "dmp");
                super.buildSplicingSQLDTO("dit.status", QueryConditionEnum.IN_LIST, waitFinishStatus, QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}

