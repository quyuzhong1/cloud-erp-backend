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
                super.buildDefaultDTO("sdn.outstock_notice_status", IsAllowOutstockEnum.WAIT_NOTICE.getCode());
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE.getCode());
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, false, QueryDataTypeEnum.BOOLEAN);
            }
            if ("unShipped".equals(value)) {
                super.buildDefaultDTO("sdn.outstock_notice_status", IsAllowOutstockEnum.PERMIT.getCode());
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
                super.buildDefaultDTO("pt.packing_status", PackingTaskStatusEnum.PACKED.getCode());
            }else{
                super.buildSplicingSQLDTO("pt.packing_status",QueryConditionEnum.NE,PackingTaskStatusEnum.PACKED.getCode() ,QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}
