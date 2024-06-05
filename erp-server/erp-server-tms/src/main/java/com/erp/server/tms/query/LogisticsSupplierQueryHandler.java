package com.erp.server.tms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnum;
import com.erp.model.tms.enums.TransferDeclareTabFlagEnum;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 物流商管理
 */
@Component
public class LogisticsSupplierQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("tab")){
            this.buildDefaultDTO("ls.type",value.toString());
        }
        return null;
    }
}

