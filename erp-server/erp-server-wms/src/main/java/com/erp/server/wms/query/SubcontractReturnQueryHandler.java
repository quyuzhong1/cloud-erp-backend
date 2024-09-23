package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @description: 委外发料
 * @author Will
 * @date: 2024/1/18 12:14
 */
@Component
public class SubcontractReturnQueryHandler extends AbstractQueryHandler {

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
        // 待提交
        if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        // 待审核
        if (ApproveStatusEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
        }
        // 已审核
        if (ApproveStatusEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        //不通过
        if (ApproveStatusEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}

