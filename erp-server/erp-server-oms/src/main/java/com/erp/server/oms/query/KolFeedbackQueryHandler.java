package com.erp.server.oms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.FeedbackStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class KolFeedbackQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    public String getTabSql(Object value) {
        if ("all".equals(value)){
            return getQueryAllSql();
        }
        // 待回片
        if (FeedbackStatusEnum.PENDING.getCode().equals(value)) {
            super.buildDefaultDTO("kf.feedback_status", FeedbackStatusEnum.PENDING.getCode());
        }
        // 已回片
        if (FeedbackStatusEnum.COMPLETED.getCode().equals(value)) {
            super.buildDefaultDTO("kf.feedback_status", FeedbackStatusEnum.COMPLETED.getCode());
        }
        return super.getSplicingSQL();
    }
}
