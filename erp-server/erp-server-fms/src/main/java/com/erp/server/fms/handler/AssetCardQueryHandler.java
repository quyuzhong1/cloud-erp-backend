package com.erp.server.fms.handler;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 资产卡片高级查询处理器
 *
 * @author wuht
 * @date 2025-10-31
 */
@Component
public class AssetCardQueryHandler extends AbstractQueryHandler {

    @Resource
    private PlmTaskFeign plmTaskFeign;
    
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if (isNameField(field)) {
            return handleNameQuery(value);
        }
        return null;
    }

    private String handleNameQuery(Object value) {
        String name = value != null ? value.toString() : null;
        if (StringUtils.isBlank(name)) {
            return null;
        }
        List<String> moldCodes = plmTaskFeign.listMoldCodesByName(name);
        if (CollectionUtils.isNotEmpty(moldCodes)) {
            super.buildDefaultDTO("acd.asset_code", moldCodes);
            return null;
        }
        return this.getQueryEmptySql();
    }

    private boolean isNameField(String field) {
        return "name".equals(field) || "ac.name".equals(field);
    }

    /**
     * 处理标签页查询
     * 
     * @param value 标签页值
     * @return SQL字符串
     */
    public String getTabSql(Object value) {
        if ("all".equals(value) || "".equals(value)) {
            return getQueryAllSql();
        }
        super.buildDefaultDTO("ac.approve_status", value);
        return super.getSplicingSQL();
    }
}
