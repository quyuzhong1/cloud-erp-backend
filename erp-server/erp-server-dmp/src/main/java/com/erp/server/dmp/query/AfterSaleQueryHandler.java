package com.erp.server.dmp.query;

import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.rpc.workflow.WorkflowFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 高级查询
 * @date 2025-04-06
 * @author jack
 */
@Component
public class AfterSaleQueryHandler extends AbstractQueryHandler {
    @Resource
    private WorkflowFeign workflowFeign;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        LoginUser user = UserContext.getNonLoginUser();
        //客户买家寄出快递单号
        if(field.equals("customer_track_no")){
            return " afs.id in (select main_id from after_sale_progress where node ='toBeReturned' and track_no "+compareCodeSplicingValueSql+")";

        }
        //商家寄出快递单号
        if(field.equals("send_track_no")){
            return " afs.id in (select main_id from after_sale_progress where node ='toBeShipped' and track_no "+compareCodeSplicingValueSql+")";
        }
        if(field.equals("tab")){
            if(value.equals("all") || value.equals("")){
                return "";
            }
            if(value.equals("waitSubmit")){
                return "(afs.approve_status = 'waitSubmit')";
            }
            if(value.equals("approveIng")){
                return "(afs.approve_status = 'approveIng')";
            }
            if(value.equals("reject")){
                return "(afs.approve_status = 'reject')";
            }
            if(value.equals("approve")){
                return "(afs.approve_status = 'approve')";
            }
        }
        return "";
    }
}
