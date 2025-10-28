package com.erp.server.dmp.query;

import org.springframework.stereotype.Component;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.dmp.enums.DmpPushMonitorTabEnum;

@Component
public class AdsPushTaskQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            } else if (DmpPushMonitorTabEnum.HISTORY.getCode().equals(searchType)) {
            	return " 1 = 2 ";
            } else if (DmpPushMonitorTabEnum.NO_NEED_SYNC.getCode().equals(searchType)) {
                return " t.push_status = 'self' ";
            } else if (DmpPushMonitorTabEnum.BLACK.getCode().equals(searchType)) {
            	return " t.push_status = 'black' ";
            } else if (DmpPushMonitorTabEnum.PUSH_ING.getCode().equals(searchType)) {
            	return " t.status in ('mqsuccess' , 'mqerror' , 'cosumererror') ";
            } else if (DmpPushMonitorTabEnum.FINISH.getCode().equals(searchType)) {
            	return " t.status = 'finish' and t.push_status = 'push' ";
            } else {
                return " t.status = '" + searchType + "' ";
            }
        }

        return null;
    }
}
