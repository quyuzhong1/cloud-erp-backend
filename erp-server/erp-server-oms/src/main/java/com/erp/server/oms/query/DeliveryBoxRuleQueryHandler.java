package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @Author: wtr
 * @Date: 2025/11/24 15:13
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class DeliveryBoxRuleQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("an.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }else if ("approveIng".equals(status)){
                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }else if ("waitCreate".equals(status)){
                return "an.approve_status = 'approve' and and1.create_po_type in ('0','1')";
            }else if ("created".equals(status)){
                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                super.buildDefaultDTO("and1.create_po_type", Arrays.asList(CreatePoTypeEnum.ALL_GENERATED.getStatus()));
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }
        //TODO

        return null;
    }
}