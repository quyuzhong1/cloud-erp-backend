package com.erp.server.tms.query;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class LogisticsBillCostQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        //差异字段查询
        if ("diffOption".equals(field) && ObjectUtil.isNotEmpty(value)) {
            if (CharSequenceUtil.equals(value.toString(),"greater")) {
                return "lbc.diff_shipping_cost > 0";
            }
            if (CharSequenceUtil.equals(value.toString(),"less")) {
                return "0 > lbc.diff_shipping_cost";
            }
            if (CharSequenceUtil.equals(value.toString(),"equal")) {
                return "lbc.diff_shipping_cost = 0";
            }
        }
        return null;
    }

    /**
     * tabSql拼接
     */
    public String getTabSql(Object value) {
        //查询自发货物流费用
        super.buildDefaultDTO("lbc.type", DictCostAttributionEnum.SELF_DELIVER.getCode());

        // 待确认
        if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(value)) {
            super.buildDefaultDTO("lbc.reconciliation_status", Collections.singletonList(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()));
        }
        // 已确认
        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(value)) {
            super.buildDefaultDTO("lbc.reconciliation_status", Collections.singletonList(ReconciliationStatusEnum.CONFIRMED.getCode()));
        }
        return super.getSplicingSQL();
    }
}

