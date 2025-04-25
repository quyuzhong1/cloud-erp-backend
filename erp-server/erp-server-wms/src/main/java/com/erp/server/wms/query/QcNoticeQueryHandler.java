package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcNoticeStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class QcNoticeQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql
     * @author jack
     * @date: 2025-04-21
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if (ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(value) || ApproveStatusEnum.APPROVE_ING.getStatus().equals(value)) {
            super.buildDefaultDTO("qn.approve_status", value);
        }
        if (QcNoticeStatusEnum.WAIT.getCode().equals(value)
                ||QcNoticeStatusEnum.PART.getCode().equals(value)
                ||QcNoticeStatusEnum.FINISH.getCode().equals(value) ) {
            super.buildDefaultDTO("qn.qc_status", value);
        }
        return super.getSplicingSQL();
    }
}
