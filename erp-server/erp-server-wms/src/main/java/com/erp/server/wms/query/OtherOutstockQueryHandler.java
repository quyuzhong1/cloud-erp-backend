package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.scm.enums.PoTableFlagEnum;
import org.springframework.stereotype.Component;

/**
 * @description: 其他入库查询
 * @author Will
 * @date: 2024/2/26 14:56
 */
@Component
public class OtherOutstockQueryHandler extends AbstractQueryHandler {

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
        // 待审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("oo.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        // 已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("oo.approve_status", ApproveStatusEnum.APPROVE.getStatus());
        }
        //不通过
        if (PoTableFlagEnum.APPROVE_REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("oo.approve_status", ApproveStatusEnum.REJECT.getStatus());
        }
        return super.getSplicingSQL();
    }
}

