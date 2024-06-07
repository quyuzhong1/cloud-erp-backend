package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.core.utils.date.LocalDateUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VirtualTransFlowQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("dateTab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/06/07 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 今天
        if ("today".equals(value)) {
            LocalDateTime dateTime = LocalDateUtil.getStartTime(LocalDateTime.now());
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GT, dateTime, QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, dateTime.plusDays(1L), QueryDataTypeEnum.DATE);
        }
        // 昨天
        if ("yesterday".equals(value)) {
            LocalDateTime dateTime = LocalDateUtil.getStartTime(LocalDateTime.now());
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GT, dateTime.minusDays(1L), QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, dateTime, QueryDataTypeEnum.DATE);
        }
        // 近七天
        if ("week".equals(value)) {
            LocalDateTime dateTime = LocalDateUtil.getStartTime(LocalDateTime.now());
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GT, dateTime.minusDays(7L), QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, dateTime, QueryDataTypeEnum.DATE);
        }
        //近一个月
        if ("month".equals(value)) {
            LocalDateTime dateTime = LocalDateUtil.getStartTime(LocalDateTime.now());
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GT, dateTime.minusDays(30L), QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, dateTime, QueryDataTypeEnum.DATE);
        }
        return super.getSplicingSQL();
    }
}
