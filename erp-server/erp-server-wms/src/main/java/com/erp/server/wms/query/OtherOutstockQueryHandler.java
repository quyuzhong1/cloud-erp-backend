package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @description: 其他出库查询
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
        if("trackNo".equals(field)){
            return getTrackNoSql(value, compareCodeSplicingValueSql);
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
        if (PageListTypeEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("oo.approve_status", ApproveStatusEnum.REJECT.getStatus());
        }
        return super.getSplicingSQL();
    }

    /**
     * @description: 跟踪号查询SQL
     * @author system
     * @date: 2025/12/16
     * @param value 查询值
     * @param compareCodeSplicingValueSql 比较条件SQL
     * @return String
     */
    public String getTrackNoSql(Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        
        // 支持的查询条件：等于、包含、IN、以...开头、以...结尾
        if(queryConditionEnum.equals(QueryConditionEnum.EQ) 
                || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) 
                || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) 
                || queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
            return "EXISTS (SELECT 1 FROM other_outstock_track_no ootn WHERE ootn.other_outstock_id = oo.id " +
                    "AND ootn.is_deleted = false AND ootn.track_no " + compareCodeSplicingValueSql + ")";
        }
        
        // 支持的反向查询条件：不等于、不包含、NOT IN
        if(queryConditionEnum.equals(QueryConditionEnum.NE) 
                || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) 
                || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
            return "NOT EXISTS (SELECT 1 FROM other_outstock_track_no ootn WHERE ootn.other_outstock_id = oo.id " +
                    "AND ootn.is_deleted = false AND ootn.track_no " + compareCodeSplicingValueSql + ")";
        }
        
        return null;
    }
}

