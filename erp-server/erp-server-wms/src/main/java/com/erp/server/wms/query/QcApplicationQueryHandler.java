package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @Author: will
 * @Date: 2026/03/25 09:58
 **/
@Component
public class QcApplicationQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if("qcStatus".equals(field)){
            if (value.equals("wait")) {
                return " (qnd.qc_status is null or qnd.qc_status "+ compareCodeSplicingValueSql +" ) ";
            } else {
                return " qnd.qc_status "+ compareCodeSplicingValueSql +" ";
            }
        }
        return null;
    }


    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2026/03/25 09:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 待提交
        if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        //审核中
        if (ApproveStatusEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getCode()));
        }
        //审核通过
        if (ApproveStatusEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getCode()));
        }
        //不通过
        if (ApproveStatusEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}