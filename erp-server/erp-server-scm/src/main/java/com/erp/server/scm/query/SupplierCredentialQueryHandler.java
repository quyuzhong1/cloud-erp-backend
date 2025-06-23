package com.erp.server.scm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;


@Component
public class SupplierCredentialQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
//        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
//        if ("supplierCode".equals(field)) {
//            String field1 = getSupplierId(field,value, queryConditionEnum);
//            if (field1 != null) return field1;
//        }
//        if ("supplierName".equals(field)) {
//            return "sc.supplier_id IN (SELECT supplier_id  FROM  supplier  WHERE  is_deleted=FALSE   AND  name ILIKE '%" + field + "%' )";
//        }
        return null;
    }

    private static String getSupplierId(String field,Object value, QueryConditionEnum queryConditionEnum) {
        String compareCode = queryConditionEnum.getCompareCode();
        if (QueryConditionEnum.EQ.equals(queryConditionEnum)) {
            return "sc.supplier_id IN (SELECT supplier_id  FROM  supplier  WHERE  is_deleted=FALSE  AND "+field +" " +compareCode+ " '" + value + "' )";
        } else if (QueryConditionEnum.NE.equals(queryConditionEnum)) {
            return "sc.supplier_id IN (SELECT supplier_id  FROM  supplier  WHERE  is_deleted=FALSE  AND "+field +" " +compareCode+ " '" + value + "' )";
        } else if (QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
            return "sc.supplier_id IN (SELECT supplier_id  FROM  supplier  WHERE  is_deleted=FALSE  AND "+field +" " +compareCode+ " '" + value + "' )";
        } else if (QueryConditionEnum.NOT_IN_LIST.equals(queryConditionEnum)) {

        } else if(QueryConditionEnum.CONTAINS.equals(queryConditionEnum)){

        } else if(QueryConditionEnum.NOT_CONTAINS.equals(queryConditionEnum)){

        } else if(QueryConditionEnum.STARTS_WITH.equals(queryConditionEnum)){

        } else if(QueryConditionEnum.ENDS_WITH.equals(queryConditionEnum)){

        } else if(QueryConditionEnum.IS_NULL.equals(queryConditionEnum)){

        } else if(QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)){

        }
        return "1 = 1";
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
        return "sc.status ='"+ value+"'";
    }
}
