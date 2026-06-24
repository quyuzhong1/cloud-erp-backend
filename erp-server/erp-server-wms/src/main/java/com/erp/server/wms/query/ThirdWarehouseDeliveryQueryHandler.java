package com.erp.server.wms.query;

import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import org.springframework.stereotype.Component;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/5/22
 */
@Component
public class ThirdWarehouseDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        if ("sb.shipping_order_no".equals(field)) {
            String tableName = "so_b2c";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c";
            }
            return " EXISTS (SELECT 1 from " + tableName + " sb where twd.so_id = sb.id and sb.is_deleted = false and sb.shipping_order_no " + compareCodeSplicingValueSql + ")";
        }
        if ("sb.so_outstock_date".equals(field)) {
            String tableName = "so_b2c";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c";
            }
            return " EXISTS (SELECT 1 from " + tableName + " sb where twd.so_id = sb.id and sb.is_deleted = false and sb.so_outstock_date " + compareCodeSplicingValueSql + ")";
        }
        if ("sbl.track_no".equals(field)) {
            String tableName = "so_b2c_logistics";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c_logistics";
            }
            return " EXISTS (SELECT 1 from " + tableName + " sbl where sbl.main_id = twd.so_id and sbl.is_deleted = false and sbl.track_no " + compareCodeSplicingValueSql + ")";
        }
        if ("sbl.code".equals(field)) {
            String tableName = "so_b2c_logistics";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c_logistics";
            }
            return " EXISTS (SELECT 1 from " + tableName + " sbl where sbl.main_id = twd.so_id and sbl.is_deleted = false and sbl.code " + compareCodeSplicingValueSql + ")";
        }
        if ("sbl.logistics_channel_id".equals(field)) {
            String tableName = "so_b2c_logistics";
            if (DynamicDataSourceTypeEnum.DORIS.equals(dynamicDataSourceTypeEnum)) {
                tableName = "erp_oms.so_b2c_logistics";
            }
            return " EXISTS (SELECT 1 from " + tableName + " sbl where sbl.main_id = twd.so_id and sbl.is_deleted = false and sbl.logistics_channel_id " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }


}
