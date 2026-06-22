package com.erp.server.wms.sdk.delivery;

import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.enums.PlatformDictEnum;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.erp.oms.aliexpress.util.ApiException;
import org.springframework.stereotype.Component;

/**
 * 速卖通海外托管自发货标发使用本地履约接口，子单字段为child_order_id。
 */
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.ALI_EXPRESS_OVERSEAS_MANAGED)
public class AliExpressOverseasManagedShipOrder extends AliexpressShipOrder {

    @Override
    protected String getLogisticsPlatformCode() {
        return PlatformDictEnum.ALI_EXPRESS_OVERSEAS_MANAGED.getCode();
    }

    @Override
    protected String getSubTradeOrderIndex(OrderItemDetail orderItemDetail) {
        return orderItemDetail.getChildOrderId();
    }

    @Override
    protected void declareDeliver(DeclareDeliverRequest request) throws ApiException {
        aliExpressOrderService.overseasManagedSubDeclareDeliver(request);
    }
}
