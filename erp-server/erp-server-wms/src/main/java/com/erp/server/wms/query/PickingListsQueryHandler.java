package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @Author: wtr
 * @Date: 2025/12/24 14:29
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class PickingListsQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("pd.sku_no".equals(field)){
            //使用exist判断picking_detail中sku_no是否符合条件
                return " exists (select 1 from picking_detail pd1 where pd1.main_id = pl.id and pd1.is_deleted=false and pd1.sku_no " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }
}
