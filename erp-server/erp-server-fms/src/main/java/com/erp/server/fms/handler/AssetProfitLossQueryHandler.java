package com.erp.server.fms.handler;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


@Component
public class AssetProfitLossQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if (isAssetNameField(field)) {
            super.buildDefaultDTO("ac.name", value);
            return super.getSplicingSQL();
        }
        return null;
    }

    private boolean isAssetNameField(String field) {
        return "assetName".equals(field) || "apld.asset_name".equals(field);
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        super.buildDefaultDTO("apl.approve_status", value);
        return super.getSplicingSQL();
    }
}
