package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class ContractInfoQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql拼接
     * @author jack
     * @date: 2025-06-26
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }

        if(Objects.equals(value.toString(), ApproveStatusEnum.APPROVE_ING.getCode())){
            LoginUser loginUser = UserContext.getNonLoginUser();
            String userId = loginUser.getUid();
            return "ci.approve_status ='"+value+"' and ci.approve_user_id = '" +userId +"'";
        }else if(Objects.equals(value.toString(), ApproveStatusEnum.REJECT.getCode())){
            return "ci.approve_status ='"+value+"'";
        }
        return "ci.status ='"+value+"'";
    }
}
