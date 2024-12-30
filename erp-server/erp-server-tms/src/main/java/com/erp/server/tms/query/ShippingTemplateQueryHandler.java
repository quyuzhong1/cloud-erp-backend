package com.erp.server.tms.query;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.ReconciliationTabStatusEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ShippingTemplateQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    /**
     * tabSql拼接
     */
    public String getTabSql(Object value) {
        if("all".equals(value.toString())){
            return this.getQueryAllSql();
        }
        if("true".equals(value.toString())){
            this.buildDefaultDTO("st.disabled",true);
        }
        if("false".equals(value.toString())){
            this.buildDefaultDTO("st.disabled",false);
        }
        return super.getSplicingSQL();
    }
}

