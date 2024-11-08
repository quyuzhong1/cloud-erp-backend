package com.erp.server.tms.query;

import cn.hutool.core.util.StrUtil;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 中转报关表
 */
@Component
public class FirstMileCostAllocationQueryHandler extends AbstractQueryHandler {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("reportPeriodStr".equals(field)){
            if (value instanceof String){
                String value1 = value + "-01";
                this.buildDefaultDTO("rpm.month", Collections.singletonList(LocalDate.parse(value1)));
            }else if (value instanceof List){
                List<LocalDate> values = new ArrayList<>();
                for (String s : (List<String>)value) {
                    if (StrUtil.isNotBlank(s)){
                        String value1 = s + "-01";
                        values.add(LocalDate.parse(value1, dateTimeFormatter));
                    }
                }
                this.buildDefaultDTO("rpm.month", values);
            }
        }
        return null;
    }

    private String getTabSql(Object value) {
        if (Objects.equals("", value) || Objects.equals("all", value)){
            return this.getQueryAllSql();
        }else {
            this.buildDefaultDTO("a.status", value);
        }
        return super.getSplicingSQL();
    }
}

