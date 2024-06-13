package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.core.utils.date.DateUtil;
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
        LocalDateTime dateTime = LocalDateUtil.getStartTime(LocalDateTime.now());
        //当天
        String date = LocalDateUtil.formatTime(dateTime, DateUtil.fmt_day);
        // 今天
        if ("today".equals(value)) {
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GE, date, QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT,  LocalDateUtil.formatTime(dateTime.plusDays(1L), DateUtil.fmt_day), QueryDataTypeEnum.DATE);
        }
        // 昨天
        if ("yesterday".equals(value)) {
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GE,  LocalDateUtil.formatTime(dateTime.minusDays(1L), DateUtil.fmt_day), QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, date, QueryDataTypeEnum.DATE);
        }
        // 近七天
        if ("week".equals(value)) {
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GE,  LocalDateUtil.formatTime(dateTime.minusDays(7L), DateUtil.fmt_day), QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, date, QueryDataTypeEnum.DATE);
        }
        //近一个月
        if ("month".equals(value)) {
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.GT,  LocalDateUtil.formatTime(dateTime.minusDays(30L), DateUtil.fmt_day), QueryDataTypeEnum.DATE);
            super.buildSplicingSQLDTO("vtf.trade_time", QueryConditionEnum.LT, date, QueryDataTypeEnum.DATE);
        }
        return super.getSplicingSQL();
    }
}
