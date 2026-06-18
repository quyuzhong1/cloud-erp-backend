package com.erp.server.fms.handler;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AssetAcceptQueryHandler extends AbstractQueryHandler {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if (isProductNameField(field)) {
            return handleProductNameQuery(value);
        }
        return null;
    }

    private String handleProductNameQuery(Object value) {
        String productName = value != null ? value.toString() : null;
        if (StringUtils.isBlank(productName)) {
            return null;
        }
        List<String> moldCodes = plmTaskFeign.listMoldCodesByName(productName);
        if (CollectionUtils.isNotEmpty(moldCodes)) {
            super.buildDefaultDTO("aad.sku_no", moldCodes);
            return null;
        }
        return this.getQueryEmptySql();
    }

    private boolean isProductNameField(String field) {
        return "productName".equals(field) || "aad.product_name".equals(field);
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        super.buildDefaultDTO("aa.approve_status", value);
        return super.getSplicingSQL();
    }
}
