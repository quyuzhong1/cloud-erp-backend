package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/5/22
 */
@Component
public class ShopQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("name".equals(field)) {
            super.buildDefaultDTO("name", value);
        }
        if ("account".equals(field)) {
            super.buildDefaultDTO("account", value);
        }
        if ("dict_platform".equals(field)) {
            super.buildDefaultDTO("dict_platform", value);
        }
        if ("sales_org_id".equals(field)) {
            super.buildDefaultDTO("sales_org_id", value);
        }
        return null;
    }

    /**
     * @param value
     * @return String
     * @description: tabSql
     * @author hyj
     * @date 2024/5/23
     */
    public String getTabSql(Object value) {
        // overseas 海外, internal 国内
        if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("wlmi.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        // 待审核
        if (ApproveStatusEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("wlmi.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        // 已审核
        if (ApproveStatusEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("wlmi.approve_status", ApproveStatusEnum.APPROVE.getStatus());
        }
        //不通过
        if (ApproveStatusEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("wlmi.approve_status", ApproveStatusEnum.REJECT.getStatus());
        }
        return super.getSplicingSQL();
    }

}
