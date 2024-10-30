package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class SoDeliveryNoticeQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)) {
            if ("waitSubmit".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getCode());
            }
            if ("toBeApprove".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE_ING.getCode());
            }
            if ("unShipped".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE.getCode());
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, false, QueryDataTypeEnum.BOOLEAN);
            }
            if ("reject".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.REJECT.getCode());
            }
            if ("completeShipment".equals(value)) {
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, true, QueryDataTypeEnum.BOOLEAN);
            }
        }

        if("isPacked".equals(field)) {
            if((Boolean) value){
                return "  sdn.id in ( SELECT main_id FROM so_delivery_notice_detail sdnd WHERE sdnd.is_deleted = FALSE " +
                        "    GROUP BY main_id HAVING COUNT(*) = COUNT(     CASE " +
                        "            WHEN (SELECT COALESCE(SUM(pd.picked_qty), 0) " +
                        "                  FROM picking_detail pd " +
                        "                  WHERE pd.is_deleted = FALSE AND pd.source_detail_id = sdnd.id) = sdnd.delivery_qty " +
                        "            THEN 1 " +
                        "            ELSE NULL " +
                        "        END " +
                        "    ) " +
                        " )";
            }else{
                return "  sdn.id not in ( SELECT main_id FROM so_delivery_notice_detail sdnd WHERE sdnd.is_deleted = FALSE " +
                        "    GROUP BY main_id HAVING COUNT(*) = COUNT(     CASE " +
                        "            WHEN (SELECT COALESCE(SUM(pd.picked_qty), 0) " +
                        "                  FROM picking_detail pd " +
                        "                  WHERE pd.is_deleted = FALSE AND pd.source_detail_id = sdnd.id) = sdnd.delivery_qty " +
                        "            THEN 1 " +
                        "            ELSE NULL " +
                        "        END " +
                        "    ) " +
                        " )";
            }
        }
        return null;
    }
}
