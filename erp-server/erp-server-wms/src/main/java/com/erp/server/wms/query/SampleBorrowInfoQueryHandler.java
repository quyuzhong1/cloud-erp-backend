package com.erp.server.wms.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SampleBorrowInfoQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }

        if(Objects.equals(value, ApproveStatusEnum.APPROVE.getCode())){ //待归还
            super.buildSplicingSQLDTO("sbd.wait_return_qty", QueryConditionEnum.GT,0, QueryDataTypeEnum.NUMBER);
            super.buildDefaultDTO("sbi.approve_status", value);
        }else if(Objects.equals(value, ApproveStatusEnum.WAIT_SUBMIT.getCode()+"/"+ApproveStatusEnum.REJECT.getCode())){ //待提交/审核不通过
            return "sbi.approve_status in ('"+ApproveStatusEnum.WAIT_SUBMIT.getCode()+"','"+ApproveStatusEnum.REJECT.getCode()+"')";
        }else {
            super.buildDefaultDTO("sbi.approve_status", value);
        }
        return super.getSplicingSQL();
    }
}
