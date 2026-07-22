package com.erp.server.fms.handler;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AssetProfitLossQueryHandler extends AbstractQueryHandler {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        // 资产名称：按模具档案名称查编码，再按明细 asset_code 过滤（不依赖本地冗余名）
        if (isAssetNameField(field)) {
            return handleAssetNameQuery(value);
        }
        return null;
    }

    private String handleAssetNameQuery(Object value) {
        String assetName = value != null ? value.toString() : null;
        if (StringUtils.isBlank(assetName)) {
            return null;
        }
        List<String> moldCodes = plmTaskFeign.listMoldCodesByName(assetName);
        if (CollectionUtils.isNotEmpty(moldCodes)) {
            super.buildDefaultDTO("apld.asset_code", moldCodes);
            return null;
        }
        return this.getQueryEmptySql();
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
