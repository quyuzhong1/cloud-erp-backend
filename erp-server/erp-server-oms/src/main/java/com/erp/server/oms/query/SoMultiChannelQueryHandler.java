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
        super.buildDefaultDTO("smc.approve_status", value.toString());
        return super.getSplicingSQL();
    }
}

