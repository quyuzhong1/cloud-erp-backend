package com.erp.server.dmp.query;

import com.common.business.enums.SyncStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;

import java.util.Arrays;

public class DmpOutputTaskRecordQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType) ||"-1".equals(searchType)  ) {
                return getQueryAllSql();
            }

            if (SyncStatusEnum.NO_NEED_SYNC.getCode().equals(searchType)) {
                super.buildDefaultDTO("t.status", Arrays.asList(DmpOutputTaskRecordStatusEnum.FINISH.getCode()));
                super.buildDefaultDTO("t.is_need_sync", Boolean.FALSE);

            } if (SyncStatusEnum.IN_SYNC.getCode().equals(searchType)) {
                super.buildDefaultDTO("t.status", Arrays.asList(
                        DmpOutputTaskRecordStatusEnum.INIT.getCode(),
                        DmpOutputTaskRecordStatusEnum.MQSUCCESS.getCode())
                );
            } if (SyncStatusEnum.SUCCESS_SYNC.getCode().equals(searchType)) {
                super.buildDefaultDTO("t.status", Arrays.asList(DmpOutputTaskRecordStatusEnum.FINISH.getCode()));
            } if (SyncStatusEnum.FAILED_SYNC.getCode().equals(searchType)) {
                super.buildDefaultDTO("t.status", Arrays.asList(
                        DmpOutputTaskRecordStatusEnum.ERROR.getCode(),
                        DmpOutputTaskRecordStatusEnum.MQERROR.getCode(),
                        DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode())
                );
            } else{
                super.buildDefaultDTO("t.status",searchType);
            }
        }
        return null;
    }
}
