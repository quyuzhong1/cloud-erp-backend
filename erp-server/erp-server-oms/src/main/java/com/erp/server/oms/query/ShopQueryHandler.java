package com.erp.server.oms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/5/22
 */
@Component
public class ShopQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("name".equals(field)) {
            super.buildDefaultDTO("name", value);
        }
        if ("account".equals(field)) {
            super.buildDefaultDTO("account", value);
        }
        if ("dict_platform".equals(field)) {
            super.buildDefaultDTO("dict_platform", value);
        }
        if ("sales_org_id".equals(field)) {
            super.buildDefaultDTO("sales_org_id", value);
        }
        return null;
    }


}
