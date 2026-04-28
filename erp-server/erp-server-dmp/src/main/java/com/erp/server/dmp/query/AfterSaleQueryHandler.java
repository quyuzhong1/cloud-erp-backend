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

        if(field.equals("afs.cs_agent_id")){
            if (compareCodeSplicingValueSql.startsWith("=")) {
                return " afs.cs_agent_id like " + "'%" + compareCodeSplicingValueSql.substring(compareCodeSplicingValueSql.indexOf("'") + 1, compareCodeSplicingValueSql.lastIndexOf("'")) + "%'" ;
            } else if (compareCodeSplicingValueSql.startsWith("in")){
                String content = compareCodeSplicingValueSql.replace("in (", "").replace(")", "");

                String[] values = content.split("','");

                StringBuilder result = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    String s = values[i].replace("'", "");
                    if (i > 0) {
                        result.append(" or ");
                    }
                    result.append(" afs.cs_agent_id like ").append("'%").append(s).append("%'");
                }
                return result.insert(0,"(").append(")").toString();
            } else if (compareCodeSplicingValueSql.startsWith("!=")){
                return " afs.cs_agent_id not like " + "'%" + compareCodeSplicingValueSql.substring(compareCodeSplicingValueSql.indexOf("'") + 1, compareCodeSplicingValueSql.lastIndexOf("'")) + "%'" ;
            } else if (compareCodeSplicingValueSql.startsWith("not in")){
                String content = compareCodeSplicingValueSql.replace("not in (", "").replace(")", "");

                String[] values = content.split("','");

                StringBuilder result = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    String s = values[i].replace("'", "");
                    if (i > 0) {
                        result.append(" and ");
                    }
                    result.append(" afs.cs_agent_id like ").append("'%").append(s).append("%'");
                }
                return result.toString();
            }
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
