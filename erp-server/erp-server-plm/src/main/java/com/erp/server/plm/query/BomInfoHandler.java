package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.server.plm.constant.SearchType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
public class BomInfoHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("b.state".equals(field)) {
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if (QueryConditionEnum.EQ.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.EQ, value, QueryDataTypeEnum.NUMBER);
            } else if (QueryConditionEnum.NE.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.NE, value, QueryDataTypeEnum.NUMBER);
            } else if (QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.IN_LIST, value, QueryDataTypeEnum.NUMBER);
            } else if (QueryConditionEnum.NOT_IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.NOT_IN_LIST, value, QueryDataTypeEnum.NUMBER);
            }
        }
        return null;
    }

    public String getTabSql(Object value) {
        // 待提交
        if (SearchType.WAIT_AUDIT.equals(value)) {
            super.buildDefaultDTO("b.state", Collections.singletonList(BomStateEnum.WAIT_AUDIT.getState()));
        }
        return super.getSplicingSQL();
    }
}
