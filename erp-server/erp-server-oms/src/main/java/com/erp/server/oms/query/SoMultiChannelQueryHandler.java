package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.CreateStatusEnum;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author Will
 * @date: 2024/2/28 9:49
 */
@Component
public class SoMultiChannelQueryHandler extends AbstractQueryHandler {

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
        if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value) || ApproveStatusEnum.APPROVE_ING.getCode().equals(value)  || ApproveStatusEnum.REJECT.getCode().equals(value)){
            super.buildDefaultDTO("smc.approve_status", value.toString());
        }else if (CreateStatusEnum.CREATING.getCode().equals(value) || CreateStatusEnum.SUCCESS.getCode().equals(value)){
            super.buildDefaultDTO("smc.approve_status", ApproveStatusEnum.APPROVE.getCode());
            super.buildDefaultDTO("smc.create_status", value.toString());
        }else if (InvalidStatusEnum.VOIDED.getStatus().toString().equals(value)){
            super.buildSplicingSQLDTO("smc.invalid_status", QueryConditionEnum.EQ, InvalidStatusEnum.VOIDED.getStatus(), QueryDataTypeEnum.BOOLEAN);
        }
        return super.getSplicingSQL();
    }
}

