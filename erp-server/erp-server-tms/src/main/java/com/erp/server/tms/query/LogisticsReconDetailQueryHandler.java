package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 物流商对账明细（行级）高级查询
 * @author Will
 * @date 2026/6/1 10:30
 */
@Component
public class LogisticsReconDetailQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("tab")){
            return getTabSql(value);
        }
        return "";
    }

    /**
     * tab查询
     * @author Will
     * @date: 2026/06/18
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 未匹配
        if (LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode().equals(value)) {
            super.buildDefaultDTO("sub.match_status", Collections.singletonList(LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode()));
        }
        // 匹配中
        if (LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(value)) {
            super.buildDefaultDTO("sub.match_status", Collections.singletonList(LogisticsReconDetailMatchStatusEnum.MATCHING.getCode()));
        }
        // 已匹配
        if (LogisticsReconDetailMatchStatusEnum.MATCHED.getCode().equals(value)) {
            super.buildDefaultDTO("sub.match_status", Collections.singletonList(LogisticsReconDetailMatchStatusEnum.MATCHED.getCode()));
        }
        // 匹配失败
        if (LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(value)) {
            super.buildDefaultDTO("sub.match_status", Collections.singletonList(LogisticsReconDetailMatchStatusEnum.FAILED.getCode()));
        }
        return super.getSplicingSQL();
    }

}
