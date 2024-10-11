package com.erp.server.dmp.query;

import com.common.business.enums.SyncStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.DmpPushMonitorTabEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DmpOutputTaskRecordQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType) || "-1".equals(searchType)) {
                return getQueryAllSql();
            }

            if (DmpPushMonitorTabEnum.NO_NEED_SYNC.getCode().equals(searchType)) {
                return "t.status = '" + DmpOutputTaskRecordStatusEnum.FINISH.getCode() + "' AND t.is_need_sync = " + Boolean.FALSE + "";
            } else if (DmpPushMonitorTabEnum.PUSH_ING.getCode().equals(searchType)) {
                return "(t.status = '" + DmpOutputTaskRecordStatusEnum.MQSUCCESS.getCode() + "'" +
                        " OR t.status = '" + DmpOutputTaskRecordStatusEnum.MQERROR.getCode() + "'" +
                        " OR t.status = '" + DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode() + "')";
            } else if (DmpPushMonitorTabEnum.BLACK.getCode().equals(searchType)) {
                return " dcob.ID IS NOT NULL and dcob.field_value != '' ";
            } else {
                super.buildDefaultDTO("t.status", searchType);
            }
        }


        return null;
    }
}
