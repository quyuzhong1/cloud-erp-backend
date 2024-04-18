package com.erp.server.scm.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author zdy
 * @date 2024年01月08日 9:54
 */
@Component
public class OrderConfirmQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            if("all".equals(value)){
                return " po.approve_status = 'approve' ";
            }
            if(ExecutionStatusEnum.TO_BE_CONFIRM.getCode().equals(value)){
                return " po.approve_status = 'approve' and pod.execution_status = 'toBeConfirm'";
            }
            if(ExecutionStatusEnum.CONFIRM.getCode().equals(value)){
                return " po.approve_status = 'approve' and pod.execution_status = 'confirm'";
            }
            if(ExecutionStatusEnum.REJECT.getCode().equals(value)){
                return " po.approve_status = 'approve' and pod.execution_status = 'reject'";
            }
            if(ExecutionStatusEnum.DELIVERY.getCode().equals(value)){
                return " po.approve_status = 'approve' and pod.execution_status = 'delivery'";
            }
            if(ExecutionStatusEnum.FINISH.getCode().equals(value)){
                return " po.approve_status = 'approve' and pod.execution_status = 'finish'";
            }
            if(ExecutionStatusEnum.CLOSED.getCode().equals(value)){
                return " po.approve_status = 'approve' and pod.execution_status = 'closed'";
            }
        }
        return null;
    }
}

