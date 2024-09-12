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
                return "(t.status = '" + DmpOutputTaskRecordStatusEnum.MQSUCCESS.getCode() + "' " +
                        "|| t.status = " + DmpOutputTaskRecordStatusEnum.MQERROR.getCode() + "" +
                        "|| t.status = " + DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode() + ")";
            } else if (DmpPushMonitorTabEnum.BLACK.getCode().equals(searchType)) {
                return "EXISTS ( SELECT 1 FROM dmp_cfg_output_black b WHERE b.field_name = t.source_code ) ";
            } else {
                super.buildDefaultDTO("t.status", searchType);
            }
        }


        return null;
    }
}
