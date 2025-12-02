package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.IsAllowOutstockEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
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
            if ("packingCompleted".equals(value)) {
                super.buildSplicingSQLDTO("sdn.is_allow_outstock", QueryConditionEnum.EQ, IsAllowOutstockEnum.WAIT_NOTICE.getCode(), QueryDataTypeEnum.BOOLEAN);
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE.getCode());
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, false, QueryDataTypeEnum.BOOLEAN);
            }
            if ("unShipped".equals(value)) {
                super.buildSplicingSQLDTO("sdn.is_allow_outstock", QueryConditionEnum.EQ, IsAllowOutstockEnum.PERMIT.getCode(), QueryDataTypeEnum.BOOLEAN);
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
        //是否装箱
        if("isPacked".equals(field)) {
            if((Boolean) value){
                return " EXISTS (SELECT 1 FROM packing_task pt WHERE pt.source_id = sdn.id AND pt.is_deleted = FALSE and pt.packing_status = '" + PackingTaskStatusEnum.PACKED.getCode() + "')";
            }else{
                return " NOT EXISTS (SELECT 1 FROM packing_task pt WHERE pt.source_id = sdn.id AND pt.is_deleted = FALSE and pt.packing_status = '" + PackingTaskStatusEnum.PACKED.getCode() + "')";
            }
        }
        //拣货单状态
        if ("generationPickStatus".equals(field)) {
            if ("未生成".equals(value)) {
                return " total.total_picked_qty = 0 ";
            } else if ("已生成".equals(value)){
                return "total.total_delivery_qty = total.total_picked_qty";
            } else if ("部分生成".equals(value)){
                return "total.total_delivery_qty > total.total_picked_qty AND total.total_picked_qty > 0";
            }
        }
        return null;
    }
}
