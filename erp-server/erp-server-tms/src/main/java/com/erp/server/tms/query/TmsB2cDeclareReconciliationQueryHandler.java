package com.erp.server.tms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class TmsB2cDeclareReconciliationQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("tab")){
            if("all".equals(value.toString())){
                return this.getQueryAllSql();
            }
            if("registered".equals(value.toString())){
                this.buildDefaultDTO("pr.status","registered");
            }else if ("notRegister".equals(value.toString())){
                this.buildSplicingSQLDTO("pr.status", QueryConditionEnum.NE,"registered", QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}

