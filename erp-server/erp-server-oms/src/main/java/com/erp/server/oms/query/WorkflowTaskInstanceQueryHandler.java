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
        if (CharSequenceUtil.isNotBlank(value.toString()) && !"all".equals(value.toString())) {
            super.buildDefaultDTO("wti.status", value);
        }
        return super.getSplicingSQL();
    }
}
