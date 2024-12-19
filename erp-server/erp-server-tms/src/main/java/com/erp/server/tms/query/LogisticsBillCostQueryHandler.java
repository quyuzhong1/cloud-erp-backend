package com.erp.server.tms.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.ReconciliationTabStatusEnum;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;

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
        if ("lbc.pay_status".equals(field) && ObjectUtil.isNotEmpty(value)) {
        	List<String> values = new ArrayList<>();
        	if(value instanceof String) {
        		values.add(value.toString());
        	}else {
        		values = (List<String>)value;
        	}
        	List<String> payTypeList = new ArrayList<>();
        	List<String> payStatusList = new ArrayList<>();
        	//LogisticsBillCostPayStatusEnum 枚举
        	for(String v : values) {
        		String[] payTypeStatus = v.split("_");
        		payTypeList.add(payTypeStatus[0]);
        		payStatusList.add(payTypeStatus[1]);
        	}
        	if(compareCodeSplicingValueSql.startsWith(QueryConditionEnum.NE.getCode()) || compareCodeSplicingValueSql.startsWith(QueryConditionEnum.NOT_IN_LIST.getCode())) {
        		buildSplicingSQLDTO("lbc.pay_type", QueryConditionEnum.NOT_IN_LIST, payTypeList, QueryDataTypeEnum.STRING);
            	buildSplicingSQLDTO("lbc.pay_status", QueryConditionEnum.NOT_IN_LIST, payStatusList, QueryDataTypeEnum.STRING);
        	}else {
        		super.buildDefaultDTO("lbc.pay_type", payTypeList);
            	super.buildDefaultDTO("lbc.pay_status", payStatusList);
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
        if (ReconciliationTabStatusEnum.PAY_CONFIRM.getCode().equals(value)) {
            super.buildDefaultDTO("lbc.reconciliation_status", Collections.singletonList(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()));
            super.buildDefaultDTO("lbc.pay_type", Collections.singletonList("pay"));
        }
        if (ReconciliationTabStatusEnum.PAY_CONFIRMED.getCode().equals(value)) {
        	super.buildDefaultDTO("lbc.reconciliation_status", Arrays.asList(ReconciliationStatusEnum.CONFIRMED.getCode() , ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode()));
        	super.buildDefaultDTO("lbc.pay_type", Collections.singletonList("pay"));
        }
        if (ReconciliationTabStatusEnum.REFUND_CONFIRM.getCode().equals(value)) {
        	super.buildDefaultDTO("lbc.reconciliation_status", Collections.singletonList(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()));
        	super.buildDefaultDTO("lbc.pay_type", Collections.singletonList("refund"));
        }
        if (ReconciliationTabStatusEnum.REFUND_CONFIRMED.getCode().equals(value)) {
        	super.buildDefaultDTO("lbc.reconciliation_status", Arrays.asList(ReconciliationStatusEnum.CONFIRMED.getCode() , ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode()));
        	super.buildDefaultDTO("lbc.pay_type", Collections.singletonList("refund"));
        }
        
        return super.getSplicingSQL();
    }
}

