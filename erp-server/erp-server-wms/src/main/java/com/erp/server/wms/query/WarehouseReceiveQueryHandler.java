package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.utils.CollectionUtils;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.abego.treelayout.internal.util.java.util.ListUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WarehouseReceiveQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if("qcStatus".equals(field)){
            List<String> valueList = CollectionUtils.convertStrClzToList(value);
            String sql = "(";
            //是否是第一个，否则需要加连接符
            boolean isFirst = true;
            for (String s : valueList) {
                if(!isFirst){
                    sql = sql + "or";
                }
                isFirst = false;
                if("waitQc".equals(s)){
                    sql = sql + "(wrd.receive_qty = wrd.wait_qc_qty)";
                }else if ("partialQc".equals(s)){
                    sql = sql + "\t(wrd.receive_qty > wrd.wait_qc_qty and wrd.wait_qc_qty > 0)";
                }else if ("finishQc".equals(s)){
                    sql = sql +"(wrd.wait_qc_qty = 0)";
                }
            }
            sql = sql + ")";
            return sql;
        }
        return null;
    }

    public String getTabSql (Object value) {
        // 待提交
        if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("wr.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        // 待审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("wr.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        // 已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("wr.approve_status", ApproveStatusEnum.APPROVE.getStatus());
        }
        //不通过
        if (PageListTypeEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("wr.approve_status", ApproveStatusEnum.REJECT.getStatus());
        }
        return super.getSplicingSQL();
    }
}
