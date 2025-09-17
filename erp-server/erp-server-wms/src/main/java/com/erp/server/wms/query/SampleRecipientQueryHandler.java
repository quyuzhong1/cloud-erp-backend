package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class SampleRecipientQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value) || "".equals(value)) {
            return getQueryAllSql();
        }
        
        String tabFlag = value.toString();
        
        switch (tabFlag) {
            case "waitSubmit":
                // 待提交
                super.buildDefaultDTO("sr.approve_status", "waitSubmit");
                break;
            case "approveIng":
                // 审核中
                super.buildDefaultDTO("sr.approve_status", "approveIng");
                break;
            case "waitOutstock":
                // 待出库：审核通过且执行状态为待出库或部分出库
                super.buildDefaultDTO("sr.approve_status", "approve");
                super.buildSplicingSQLDTO("srd.exec_status", QueryConditionEnum.IN_LIST, 
                    java.util.Arrays.asList("waitOutstock", "partOutstock"), QueryDataTypeEnum.STRING);
                break;
            case "completeOutstock":
                // 已出库：执行状态为完全出库
                super.buildSplicingSQLDTO("srd.exec_status", QueryConditionEnum.EQ, 
                    "completeOutstock", QueryDataTypeEnum.STRING);
                break;
            case "reject":
                // 不通过
                super.buildDefaultDTO("sr.approve_status", "reject");
                break;
            case "waitSubmitOrReject":
                // 待提交/不通过：移动端合并标签，查询待提交和不通过状态
                super.buildSplicingSQLDTO("sr.approve_status", QueryConditionEnum.IN_LIST, 
                    java.util.Arrays.asList("waitSubmit", "reject"), QueryDataTypeEnum.STRING);
                break;
            default:
                // 其他情况按审核状态处理
                super.buildDefaultDTO("sr.approve_status", value);
                break;
        }
        
        return super.getSplicingSQL();
    }
}
