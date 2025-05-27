package com.erp.server.workflow.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.workflow.enums.ApproveTaskStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class ApproveTaskInfoQueryHandler extends AbstractQueryHandler {


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
     * @date: 2025/5/27 10:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 生成成功
        if (ApproveTaskStatusEnum.SUCCESS.getCode().equals(value)) {
            super.buildDefaultDTO("ati.status", Collections.singletonList(ApproveTaskStatusEnum.SUCCESS.getCode()));
        }
        // 生成失败
        if (ApproveTaskStatusEnum.FAIL.getCode().equals(value)) {
            super.buildDefaultDTO("ati.status", Collections.singletonList(ApproveTaskStatusEnum.FAIL.getCode()));
        }
        return super.getSplicingSQL();
    }
}
