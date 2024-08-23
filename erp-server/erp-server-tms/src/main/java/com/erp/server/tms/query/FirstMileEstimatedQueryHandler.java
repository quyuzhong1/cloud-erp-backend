package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 头程暂估账单高级查询处理
 * @date 2024-08-16
 * @author tanmujin
 */
@Component
public class FirstMileEstimatedQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        if (Objects.equals("", value) || Objects.equals("all", value)){
            return this.getQueryAllSql();
        }else {
            this.buildDefaultDTO("eb.status", value);
        }
        return super.getSplicingSQL();
    }
}
