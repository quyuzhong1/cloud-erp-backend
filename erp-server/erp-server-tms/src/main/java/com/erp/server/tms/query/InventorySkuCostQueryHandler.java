package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zdy
 * @ClassName InventorySkuCostQueryHandler
 * @date 2024年08月16日
 * @version: 1.0
 */
@Component
public class InventorySkuCostQueryHandler extends AbstractQueryHandler {
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
            this.buildDefaultDTO("c.status", value);
        }
        return super.getSplicingSQL();
    }
}
