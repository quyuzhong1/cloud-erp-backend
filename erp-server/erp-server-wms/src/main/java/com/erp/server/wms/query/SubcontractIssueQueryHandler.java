package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.TabFlagEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @description: 委外发料
 * @author Will
 * @date: 2024/1/18 12:14
 */
@Component
public class SubcontractIssueQueryHandler extends AbstractQueryHandler {

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
        if (TabFlagEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("si.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        // 待审核
        if (TabFlagEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("si.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
        }
        // 已审核
        if (TabFlagEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("si.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        //不通过
        if (TabFlagEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("si.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}

