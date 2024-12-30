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
            if(isContain()){
                for (String s : valueList) {
                    if(!isFirst){
                        sql = sql + "or";
                    }
                    isFirst = false;
                    if("waitQc".equals(s)){
                        sql = sql + "(not exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive' " +
                                " and qi.qc_status NOT IN ('','draft','waitQc','cancel')))";
                    }else if ("partialQc".equals(s)){
                        sql = sql + "\t( exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive' \n" +
                                "                                 and qi.qc_status NOT IN ('','draft','waitQc','cancel')))\n" +
                                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t and \n" +
                                "\t\t\t\t\t\t\t\t\t\t\t\t (     exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive' \n" +
                                "                                and qi.qc_status not IN ('exemption','finishQc')))";
                    }else if ("finishQc".equals(s)){
                        sql = sql +"( EXISTS (\n" +
                                "\t\tSELECT\n" +
                                "\t\t\t\t1 \n" +
                                "\t\t\tFROM\n" +
                                "\t\t\t\tqc_info qi \n" +
                                "\t\t\tWHERE\n" +
                                "\t\t\t\tqi.is_deleted = FALSE \n" +
                                "\t\t\t\tAND qi.source_id = wr.ID \n" +
                                "\t\t\t\tAND qi.source_type = 'poReceive' \n" +
                                "\t\t) AND  not exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive'\n" +
                                "and qi.qc_status not IN ('exemption','finishQc')))";
                    }
                }
            }else{
                for (String s : valueList) {
                    if(!isFirst){
                        sql = sql + "or";
                    }
                    isFirst = false;
                    if("waitQc".equals(s)){
                        sql = sql + "( exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive' " +
                                " and qi.qc_status NOT IN ('','draft','waitQc','cancel')))";
                    }else if ("partialQc".equals(s)){
                        sql = sql + "\t( not  exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive' \n" +
                                "                                 and qi.qc_status NOT IN ('','draft','waitQc','cancel')))\n" +
                                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t or \n" +
                                "\t\t\t\t\t\t\t\t\t\t\t\t (   not   exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive' \n" +
                                "                                and qi.qc_status not IN ('exemption','finishQc')))";
                    }else if ("finishQc".equals(s)){
                        sql = sql +"( not  EXISTS (\n" +
                                "\t\tSELECT\n" +
                                "\t\t\t\t1 \n" +
                                "\t\t\tFROM\n" +
                                "\t\t\t\tqc_info qi \n" +
                                "\t\t\tWHERE\n" +
                                "\t\t\t\tqi.is_deleted = FALSE \n" +
                                "\t\t\t\tAND qi.source_id = wr.ID \n" +
                                "\t\t\t\tAND qi.source_type = 'poReceive' \n" +
                                "\t\t) or  exists ( SELECT 1 FROM qc_info qi where qi.is_deleted  =false and qi.source_id = wr.id and qi.source_type = 'poReceive'\n" +
                                "and qi.qc_status not IN ('exemption','finishQc')))";
                    }
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
