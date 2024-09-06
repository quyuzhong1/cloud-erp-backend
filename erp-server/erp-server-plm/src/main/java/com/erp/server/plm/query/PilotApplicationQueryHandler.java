package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 试产/量产单高级查询
 * @date 2024-08-27
 * @author tanmujin
 */
@Component
public class PilotApplicationQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        LoginUser user = UserContext.getNonLoginUser();
        if(field.equals("tab")){
            if(value.equals("all") || value.equals("")){
                return "";
            }
            if(value.equals("waitSubmit")){
                return "(pa.approve_status = 'waitSubmit')";
            }
            if(value.equals("waitMeApprove")){
                return "(pa.approve_user_id = '" + user.getUid() + "')";
            }
            if(value.equals("reject")){
                return "(pa.approve_status = 'reject')";
            }
            if(value.equals("notOrder")){
                return "(pa.approve_status = 'approve' and pa.order_status = 'notOrder')";
            }
            if(value.equals("order")){
                return "(pa.approve_status = 'approve' and pa.order_status = 'order')";
            }
        }
        return "";
    }
}
