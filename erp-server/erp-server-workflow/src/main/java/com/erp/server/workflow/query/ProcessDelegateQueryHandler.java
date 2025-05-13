package com.erp.server.workflow.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class ProcessDelegateQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

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
        // 待提交
        if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("pd.status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        // 待审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("pd.status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
        }
        // 已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("pd.status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        return super.getSplicingSQL();
    }
}
