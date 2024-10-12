package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.server.plm.constant.SearchType;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class BomInfoHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql (Object value) {
        // 待提交
        if (SearchType.WAIT_AUDIT.equals(value)) {
            super.buildDefaultDTO("b.state", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}
