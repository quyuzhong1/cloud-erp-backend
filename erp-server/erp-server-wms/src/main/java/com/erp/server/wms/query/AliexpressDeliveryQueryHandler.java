package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class AliexpressDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("isSystemOut".equals(field)){
            boolean isSystemOut = (Boolean) value;
            if(isSystemOut){
                return " exists (select 1 from so_outstock so where so.so_id = ad.so_id and so.is_deleted = false ) ";
            }else{
                return " NOT exists (select 1 from so_outstock so where so.so_id = ad.so_id and so.is_deleted = false ) ";
            }
        }
        return null;
    }
}

