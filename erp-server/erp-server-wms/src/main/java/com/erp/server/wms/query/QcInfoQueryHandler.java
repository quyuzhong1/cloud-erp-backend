package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.QcBillStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class QcInfoQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }



    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //待质检
        if (QcBillStatusEnum.WAIT_QC.getCode().equals(value)) {
            super.buildDefaultDTO("qb.qc_status", Arrays.asList(QcBillStatusEnum.WAIT_QC.getCode(), QcBillStatusEnum.DRAFT.getCode()) );
        }
        //待复检
        if (QcBillStatusEnum.WAIT_RE_QC.getCode().equals(value)) {
            super.buildDefaultDTO("qb.qc_status", QcBillStatusEnum.WAIT_RE_QC.getCode());
        }
        //已质检
        if (QcBillStatusEnum.FINISH_QC.getCode().equals(value)) {
            super.buildDefaultDTO("qb.qc_status", QcBillStatusEnum.FINISH_QC.getCode());
        }
        //已取消
        if (QcBillStatusEnum.CANCEL.getCode().equals(value)) {
            super.buildDefaultDTO("qb.qc_status", QcBillStatusEnum.CANCEL.getCode());
        }
        return super.getSplicingSQL();
    }
}
