package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class SampleBackInfoQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        
        String tabFlag = value.toString();
        
        switch (tabFlag) {
            case "waitSubmitOrReject":
                // 待提交/不通过：移动端合并标签，查询待提交和不通过状态
                super.buildSplicingSQLDTO("sbi.approve_status", QueryConditionEnum.IN_LIST,
                    java.util.Arrays.asList("waitSubmit", "reject"), QueryDataTypeEnum.STRING);
                break;
            default:
                // 其他情况按审核状态处理
                super.buildDefaultDTO("sbi.approve_status", value);
                break;
        }
        
        return super.getSplicingSQL();
    }
}

