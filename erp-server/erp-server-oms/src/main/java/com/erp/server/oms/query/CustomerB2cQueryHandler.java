package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class CustomerB2cQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    public String getTabSql (Object value) {
        // 待提交
        if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        // 待审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("mi.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        // 已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("mi.approve_status", ApproveStatusEnum.APPROVE.getStatus());
        }
        //不通过
        if (PageListTypeEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("mi.approve_status", ApproveStatusEnum.REJECT.getStatus());
        }
        return super.getSplicingSQL();
    }
}
