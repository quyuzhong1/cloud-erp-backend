package com.erp.server.tms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 中转报关表
 */
@Component
public class FirstMileChangeRecordQueryHandler extends AbstractQueryHandler {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("reportPeriodStr".equals(field)){
            if (value instanceof String){
                String value1 = value + "-01";
                this.buildDefaultDTO("fmcr.report_period", Collections.singletonList(LocalDate.parse(value1)));
            }else if (value instanceof List){
                List<LocalDate> values = new ArrayList<>();
                for (String s : (List<String>)value) {
                    if (CharSequenceUtil.isNotBlank(s)){
                        String value1 = s + "-01";
                        values.add(LocalDate.parse(value1, dateTimeFormatter));
                    }
                }
                this.buildDefaultDTO("fmcr.report_period", values);
            }
        }
        return null;
    }
}

