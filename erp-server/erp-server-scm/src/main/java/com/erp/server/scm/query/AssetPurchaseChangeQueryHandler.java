package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @Author: wtr
 * @Date: 2025/10/30 9:22
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class AssetPurchaseChangeQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("apc.tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("apc.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }else if ("approveIng".equals(status)){
                super.buildDefaultDTO("apc.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }else if ("approve".equals(status)){
                super.buildDefaultDTO("apc.approve_status",ApproveStatusEnum.APPROVE.getStatus());
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("apc.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }
        //TODO

        return null;
    }
}
