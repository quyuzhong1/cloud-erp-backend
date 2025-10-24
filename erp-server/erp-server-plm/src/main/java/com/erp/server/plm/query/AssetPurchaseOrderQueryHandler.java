package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @Author: wtr
 * @Date: 2025/10/23 9:53
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class AssetPurchaseOrderQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("apo.tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("apo.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }else if ("toBeApprove".equals(status)){
                super.buildDefaultDTO("apo.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }else if ("toBeCreate".equals(status)){
                super.buildDefaultDTO("apo.approve_status",ApproveStatusEnum.APPROVE.getStatus());
            }else if ("created".equals(status)){
                super.buildDefaultDTO("apo.approve_status",ApproveStatusEnum.APPROVE.getStatus());
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("apo.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }
        //TODO

        return null;
    }
}
