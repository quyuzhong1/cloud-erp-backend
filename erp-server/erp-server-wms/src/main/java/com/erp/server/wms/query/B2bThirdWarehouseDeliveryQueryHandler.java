package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
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
        return null;
    }


}
