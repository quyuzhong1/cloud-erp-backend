package com.erp.server.workflow.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.workflow.enums.ProcessManagementTabEnum;
import com.erp.model.workflow.enums.ProcessStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;


@Component
public class ProcessManagementQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("processVersion".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pp.process_version is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pp.process_version is not null";
            }
            return " pp.process_version " + compareCodeSplicingValueSql;
        }
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2024/1/18 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 运行中
        if (ProcessManagementTabEnum.RUNNING.getCode().equals(value)) {
            super.buildDefaultDTO("pp.process_status", Collections.singletonList(ProcessStatusEnum.RUNNING.getCode()));
        }
        // 已完成
        if (ProcessManagementTabEnum.FINISH.getCode().equals(value)) {
            super.buildDefaultDTO("pp.process_status", Collections.singletonList(ProcessStatusEnum.FINISH.getCode()));
        }
        // 异常
        if (ProcessManagementTabEnum.ABNORMAL.getCode().equals(value)) {
            super.buildDefaultDTO("pp.process_status", Arrays.asList(ProcessStatusEnum.PAUSE.getCode(),ProcessStatusEnum.TERMINATION.getCode()));
        }
        return super.getSplicingSQL();
    }
}
