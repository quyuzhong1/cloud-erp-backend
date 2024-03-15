package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class SoB2cDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("sbd.logistic_type".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }
        }
        if("sbd.tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }

            // 待处理
            if (SoB2cDeliveryStatusEnum.WAIT_HANDLE.equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.WAIT_HANDLE.getStatus());
            }
            //拣货中
            if (SoB2cDeliveryStatusEnum.PICKING.equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.PICKING.getStatus());
            }
            //虚假发货
            if (SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getStatus());
            }
            //已发货
            if (SoB2cDeliveryStatusEnum.SHIPPED.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.SHIPPED.getStatus());
            }
            //取消发货
            if (SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
            }
        }
        return null;
    }
}
