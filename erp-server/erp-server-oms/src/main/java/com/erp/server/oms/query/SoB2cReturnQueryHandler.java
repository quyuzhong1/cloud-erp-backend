package com.erp.server.oms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 */
@Component
public class SoB2cReturnQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("instockStatus".equals(field)){
            String type = value.toString();
            if ("not".equals(type)) {
                return "  not exists ( select 1 from so_return_instock a inner join so_return_instock_detail b on a.id = b.main_id where a.is_deleted = false and b.is_deleted = false and b.so_return_detail_id = sbrd.id ) ";
            }else if ("partial".equals(type)){
                return "  exists (select 1 from so_return_instock a inner join so_return_instock_detail b on a.id = b.main_id where a.is_deleted = false and b.is_deleted = false and b.so_return_detail_id = sbrd.id  and sbrd.return_qty > b.real_qty ) ";
            }else if ("instocked".equals(type)){
                return "  exists (select 1 from so_return_instock a inner join so_return_instock_detail b on a.id = b.main_id where a.is_deleted = false and b.is_deleted = false and b.so_return_detail_id = sbrd.id  and sbrd.return_qty = b.real_qty ) ";
            }else if ("beyond".equals(type)){
                return "  exists (select 1 from so_return_instock a inner join so_return_instock_detail b on a.id = b.main_id where a.is_deleted = false and b.is_deleted = false and b.so_return_detail_id = sbrd.id  and sbrd.return_qty < b.real_qty ) ";
            }
        }
        if("instockTime".equals(field)){
            compareCodeSplicingValueSql = compareCodeSplicingValueSql.replace("instockTime","a.approve_time");
            return "  exists (select 1 from so_return_instock a where a.is_deleted = false and a.so_return_id = sbr.id  and a.approve_time "+compareCodeSplicingValueSql +")";

        }
        return null;
    }
}

