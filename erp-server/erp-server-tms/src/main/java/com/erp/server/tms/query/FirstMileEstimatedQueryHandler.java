package com.erp.server.tms.query;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 头程暂估账单高级查询处理
 * @date 2024-08-16
 * @author tanmujin
 */
@Component
public class FirstMileEstimatedQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //tab列表
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("lbc.reconciliation_status".equals(field) && ObjectUtil.isNotEmpty(value)) {
            return "EXISTS (select 1 from logistics_bill_cost lbc  left join tms_first_mile_reconciliation_detail tfmrd on lbc.reconciliation_id = tfmrd.main_id and tfmrd.is_deleted = false and tfmrd.\"type\" = 'actual' and tfmrd.reconciliation_count = 1 where lbc.is_deleted = false and eb.logistics_bill_id = lbc.logistics_bill_id and lbc.reconciliation_status "+ compareCodeSplicingValueSql +" ) ";
        }
        return null;
    }

    private String getTabSql(Object value) {
        if (Objects.equals("", value) || Objects.equals("all", value)){
            return this.getQueryAllSql();
        }
        if(Objects.equals("waitConfirm", value)){
//            this.buildDefaultDTO("eb.status", "waitConfirm");
//            this.buildDefaultDTO("lbc.reconciliation_status", "toBeConfirm");
            return "eb.status = 'waitConfirm' AND EXISTS (select 1 from logistics_bill_cost lbc  left join tms_first_mile_reconciliation_detail tfmrd on lbc.reconciliation_id = tfmrd.main_id and tfmrd.is_deleted = false and tfmrd.\"type\" = 'actual' and tfmrd.reconciliation_count = 1 where lbc.is_deleted = false and eb.logistics_bill_id = lbc.logistics_bill_id and lbc.reconciliation_status = 'toBeConfirm' ) ";
        }
        if(Objects.equals("confirm", value)){
//            this.buildDefaultDTO("lbc.reconciliation_status", "confirmed");
            return "EXISTS (select 1 from logistics_bill_cost lbc  left join tms_first_mile_reconciliation_detail tfmrd on lbc.reconciliation_id = tfmrd.main_id and tfmrd.is_deleted = false and tfmrd.\"type\" = 'actual' and tfmrd.reconciliation_count = 1 where lbc.is_deleted = false and eb.logistics_bill_id = lbc.logistics_bill_id and lbc.reconciliation_status = 'confirmed' ) ";
        }

        return super.getSplicingSQL();
    }
}
