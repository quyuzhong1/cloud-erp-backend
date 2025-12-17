package com.erp.server.tms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class TmsFirstMileReconciliationQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if("tfmrd.relation_code".equals(field)){
            return " EXISTS (SELECT 1 from tms_first_mile_reconciliation_detail as detail where detail.is_deleted = false and detail.main_id = tfmr.id and detail.relation_code "+ compareCodeSplicingValueSql +" ) ";
        }
        if("tfmrd.transport_no".equals(field)){
            return " EXISTS (SELECT 1 from tms_first_mile_reconciliation_detail as detail where detail.is_deleted = false and detail.main_id = tfmr.id and detail.transport_no "+ compareCodeSplicingValueSql +" ) ";
        }
        if("tfmrd.business_code".equals(field)){
            return " EXISTS (SELECT 1 from tms_first_mile_reconciliation_detail as detail where detail.is_deleted = false and detail.main_id = tfmr.id and detail.business_code "+ compareCodeSplicingValueSql +" ) ";
        }
        return null;
    }

    /**
     * tabSql拼接
     */
    public String getTabSql(Object value) {
        // 待提交
        if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("tfmr.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        // 待审核
        if (ApproveStatusEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("tfmr.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
        }
        // 已审核
        if (ApproveStatusEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("tfmr.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        //不通过
        if (ApproveStatusEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("tfmr.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}

