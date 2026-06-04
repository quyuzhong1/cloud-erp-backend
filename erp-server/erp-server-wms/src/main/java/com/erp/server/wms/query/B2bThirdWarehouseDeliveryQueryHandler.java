package com.erp.server.wms.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/5/22
 */
@Component
public class B2bThirdWarehouseDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("btd.warehouse_operation_type".equals(field)) {
            QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
            List<String> valueList = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            boolean isFirst = true;
            StringBuilder sb = new StringBuilder();
            sb.append(" ( ");

            if(compareCode.equals(QueryConditionEnum.IN_LIST)){
                for(String valueStr : valueList){
                    if(!isFirst){
                        sb.append(" or ");
                    }
                    sb.append(" string_to_array(btd.warehouse_operation_type, ',') @> ARRAY['").append(valueStr).append("']");

                    isFirst = false;
                }
                sb.append(" ) ");
                return sb.toString();
            }else if(compareCode.equals(QueryConditionEnum.NOT_IN_LIST)){
                for(String valueStr : valueList){
                    if(!isFirst){
                        sb.append(" and ");
                    }
                    sb.append(" NOT string_to_array(btd.warehouse_operation_type, ',') @> ARRAY['").append(valueStr).append("']");

                    isFirst = false;
                }
                sb.append(" ) ");
                return sb.toString();
            }else{
                super.buildSplicingSQLDTO(field, compareCode,value, QueryDataTypeEnum.STRING);
            }
        }
        if ("salesPlatformOrderCode".equals(field) || "si.platform_order_code".equals(field)) {
            return buildSoInfoQuerySql(value, compareCodeSplicingValueSql);
        }
        if ("customerPO".equals(field) || "sdpo.customer_po".equals(field)) {
            return buildCustomerPOQuerySql(value, compareCodeSplicingValueSql);
        }
        return null;
    }

    private String buildSoInfoQuerySql(Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
        if (QueryConditionEnum.IS_NULL.equals(compareCode)) {
            return " not exists (select 1 from so_info si where si.id = btd.so_id and si.is_deleted = false and coalesce(si.platform_order_code, '') != '') ";
        }
        if (QueryConditionEnum.NOT_NULL.equals(compareCode)) {
            return " exists (select 1 from so_info si where si.id = btd.so_id and si.is_deleted = false and coalesce(si.platform_order_code, '') != '') ";
        }
        if (isNegativeCompare(compareCode)) {
            return " not exists (select 1 from so_info si where si.id = btd.so_id and si.is_deleted = false and si.platform_order_code "
                    + buildPositiveCompareSql(value, compareCode) + ")";
        }
        return " exists (select 1 from so_info si where si.id = btd.so_id and si.is_deleted = false and si.platform_order_code "
                + compareCodeSplicingValueSql + ")";
    }

    private String buildCustomerPOQuerySql(Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
        if (QueryConditionEnum.IS_NULL.equals(compareCode)) {
            return " not exists (select 1 from so_detail sd where sd.id = btdd.so_detail_id and sd.is_deleted = false and coalesce(sd.customer_po, '') != '') ";
        }
        if (QueryConditionEnum.NOT_NULL.equals(compareCode)) {
            return " exists (select 1 from so_detail sd where sd.id = btdd.so_detail_id and sd.is_deleted = false and coalesce(sd.customer_po, '') != '') ";
        }
        if (isNegativeCompare(compareCode)) {
            return " not exists (select 1 from so_detail sd where sd.id = btdd.so_detail_id and sd.is_deleted = false and coalesce(sd.customer_po, '') != '' and sd.customer_po "
                    + buildPositiveCompareSql(value, compareCode) + ")";
        }
        return " exists (select 1 from so_detail sd where sd.id = btdd.so_detail_id and sd.is_deleted = false and coalesce(sd.customer_po, '') != '' and sd.customer_po "
                + compareCodeSplicingValueSql + ")";
    }

    private boolean isNegativeCompare(QueryConditionEnum compareCode) {
        return QueryConditionEnum.NE.equals(compareCode)
                || QueryConditionEnum.NOT_IN_LIST.equals(compareCode)
                || QueryConditionEnum.NOT_CONTAINS.equals(compareCode);
    }

    private String buildPositiveCompareSql(Object value, QueryConditionEnum compareCode) {
        QueryConditionEnum positiveCompareCode = compareCode;
        if (QueryConditionEnum.NE.equals(compareCode)) {
            positiveCompareCode = QueryConditionEnum.EQ;
        } else if (QueryConditionEnum.NOT_IN_LIST.equals(compareCode)) {
            positiveCompareCode = QueryConditionEnum.IN_LIST;
        } else if (QueryConditionEnum.NOT_CONTAINS.equals(compareCode)) {
            positiveCompareCode = QueryConditionEnum.CONTAINS;
        }
        AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("", positiveCompareCode, value, QueryDataTypeEnum.STRING);
        return QueryUtils.splicingCompareValueSQL(positiveCompareCode, advanceQueryDTO);
    }

}
