package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.AssetPurchaseOrderTabListEnum;
import org.springframework.stereotype.Component;

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
        if("tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("apo.approve_status", AssetPurchaseOrderTabListEnum.WAIT_SUBMIT.getStatus());
            }else if ("approveIng".equals(status)){
                super.buildDefaultDTO("apo.approve_status",AssetPurchaseOrderTabListEnum.APPROVE_ING.getStatus());
            }else if ("waitReceive".equals(status)){
                super.buildDefaultDTO("apo.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                super.buildDefaultDTO("apod.end_receive",AssetPurchaseOrderTabListEnum.WAIT_RECEIVE.getStatus());
            }else if ("allReceive".equals(status)){
                super.buildDefaultDTO("apod.end_receive",AssetPurchaseOrderTabListEnum.ALL_RECEIVE.getStatus());
            }else if ("close".equals(status)){
                super.buildDefaultDTO("apod.end_receive",AssetPurchaseOrderTabListEnum.CLOSE.getStatus());
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("apo.approve_status",AssetPurchaseOrderTabListEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }
        //TODO

        return null;
    }
}
