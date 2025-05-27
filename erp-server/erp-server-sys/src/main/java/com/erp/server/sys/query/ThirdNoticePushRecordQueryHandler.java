package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import org.springframework.stereotype.Component;


@Component
public class ThirdNoticePushRecordQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2024/1/18 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }
        if (ThirdNoticePushRecordStatusEnum.SUCCESS.getCode().equals(value)) {
            return "tnpr.status ='"+ ThirdNoticePushRecordStatusEnum.SUCCESS.getCode()+"'";
        }
        if (ThirdNoticePushRecordStatusEnum.FAILED.getCode().equals(value)) {
            return "tnpr.status ='"+ ThirdNoticePushRecordStatusEnum.FAILED.getCode()+"'";
        }
        return "";
    }
}
