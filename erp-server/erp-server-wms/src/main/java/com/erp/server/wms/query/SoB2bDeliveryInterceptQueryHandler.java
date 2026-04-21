package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.SoB2bDeliveryInterceptStatusEnum;
import org.springframework.stereotype.Component;

/**
 * B2B发货拦截单查询处理
 */
@Component
public class SoB2bDeliveryInterceptQueryHandler extends AbstractQueryHandler {

    private static final String HANDLE_STATUS_FIELD = "coalesce(nullif(sbdi.handle_status,''),'waitHandle')";
    private static final String SO_DELIVERY_CODE_FIELD = "COALESCE(NULLIF(sbdi.third_delivery_code,''),btd.code)";
    private static final String PLATFORM_ORDER_CODE_FIELD = "btd.platform_order_code";
    private static final String THIRD_WAREHOUSE_ORDER_CODE_FIELD = "sbdi.third_warehouse_order_code";
    private static final String LOGISTICS_CHANNEL_ID_FIELD = "COALESCE(NULLIF(btd.logistics_channel_id,''),sbdi.logistics_channel_id)";
    private static final String TRANSPORT_NO_FIELD = "COALESCE(NULLIF(btd.track_no,''),sbdi.transport_no)";

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if ("soDeliveryCode".equals(field)) {
            return SO_DELIVERY_CODE_FIELD + " " + compareCodeSplicingValueSql;
        }
        if ("platformOrderCode".equals(field)) {
            return PLATFORM_ORDER_CODE_FIELD + " " + compareCodeSplicingValueSql;
        }
        if ("thirdWarehouseOrderCode".equals(field)) {
            return THIRD_WAREHOUSE_ORDER_CODE_FIELD + " " + compareCodeSplicingValueSql;
        }
        if ("logisticsChannelId".equals(field)) {
            return LOGISTICS_CHANNEL_ID_FIELD + " " + compareCodeSplicingValueSql;
        }
        if ("transportNo".equals(field)) {
            return TRANSPORT_NO_FIELD + " " + compareCodeSplicingValueSql;
        }
        return null;
    }

    private String getTabSql(Object value) {
        if (SoB2bDeliveryInterceptStatusEnum.WAIT_HANDLE.getCode().equals(value)) {
            return HANDLE_STATUS_FIELD + " = 'waitHandle'";
        } else if (SoB2bDeliveryInterceptStatusEnum.HANDLE.getCode().equals(value)) {
            return HANDLE_STATUS_FIELD + " = 'handle'";
        }
        return super.getSplicingSQL();
    }
}
