package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.MarehouseMoveSourceTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/4/19 14:33
 */
@Component
public class MarehouseMoveInfoQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("sourceType".equals(field) && MarehouseMoveSourceTypeEnum.FIRST_MILE_PICKING.getCode().equals(value)) {
            return " EXISTS (select id from picking_lists where is_deleted = false and id = wlmi.source_id and source_type ='requisitionApplication')";
        }
        if ("sourceType".equals(field) && MarehouseMoveSourceTypeEnum.B2B_PICKING.getCode().equals(value)) {
            return " EXISTS (select id from picking_lists where is_deleted = false and id = wlmi.source_id and source_type = 'soDeliveryNotice')";
        }
        return null;
    }


    /**
     * @param value
     * @return String
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     */
    public String getTabSql(Object value) {
        // 待提交
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
