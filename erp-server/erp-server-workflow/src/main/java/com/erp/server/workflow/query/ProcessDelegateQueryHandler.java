package com.erp.server.workflow.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.workflow.enums.ProcessDelegateStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class ProcessDelegateQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
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
        // 待执行
        if (ProcessDelegateStatusEnum.PENDING.getCode().equals(value)) {
            super.buildDefaultDTO("pd.status", Collections.singletonList(ProcessDelegateStatusEnum.PENDING.getCode()));
        }
        // 运行中
        if (ProcessDelegateStatusEnum.RUNNING.getCode().equals(value)) {
            super.buildDefaultDTO("pd.status", Collections.singletonList(ProcessDelegateStatusEnum.RUNNING.getCode()));
        }
        // 已结束
        if (ProcessDelegateStatusEnum.ENDED.getCode().equals(value)) {
            super.buildDefaultDTO("pd.status", Collections.singletonList(ProcessDelegateStatusEnum.ENDED.getCode()));
        }
        return super.getSplicingSQL();
    }
}
