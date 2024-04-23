package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @author Will
 * @date: 2024/2/28 9:49
 */
@Component
public class SoReturnQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //待审核
        if (SoReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", ApproveStatusEnum.APPROVE_ING.getCode());
        }
        //已审核
        if (SoReturnChangeListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", ApproveStatusEnum.APPROVE.getCode());
        }
        //不通过
        if (SoReturnChangeListTypeEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", ApproveStatusEnum.REJECT.getCode());
        }
        return super.getSplicingSQL();
    }
}

