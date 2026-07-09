package com.erp.server.oms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 任务编排实例高级搜索
 */
@Component
public class WorkflowTaskInstanceQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //选项卡
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        String tabValue = value == null ? null : value.toString();
        if ("all".equals(value)) {
            return this.getQueryAllSql();
        }
        if (CharSequenceUtil.isNotBlank(tabValue)) {
            super.buildDefaultDTO("wti.status", value);
        }
        return super.getSplicingSQL();
    }
}
