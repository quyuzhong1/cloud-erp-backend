package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 *
 */
@Component
public class SupplierPhaseQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            String status = value.toString();
            if ("waitSubmit".equals(status)) {
                super.buildDefaultDTO("sp.approve_status", Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus()));
            } else if ("approveIng".equals(status)) {
                super.buildDefaultDTO("sp.approve_status", Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus()));
            } else if ("approve".equals(status)) {
                super.buildDefaultDTO("sp.approve_status", ApproveStatusEnum.APPROVE.getStatus());
            } else if ("reject".equals(status)) {
                super.buildDefaultDTO("sp.approve_status", ApproveStatusEnum.REJECT.getStatus());
            } else {
                return this.getQueryAllSql();
            }
        }
        return null;
    }
}

