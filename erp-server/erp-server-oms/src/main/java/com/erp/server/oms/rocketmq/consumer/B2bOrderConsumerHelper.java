package com.erp.server.oms.rocketmq.consumer;

import com.common.business.dto.PlatformB2bOrderDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import org.apache.commons.lang3.StringUtils;

final class B2bOrderConsumerHelper {

    private B2bOrderConsumerHelper() {
    }

    static void applyCustomerDefaults(PlatformB2bOrderDTO dto, CustomerInfoEntity customerInfo) {
        if (StringUtils.isBlank(dto.getWarehouseId()) && StringUtils.isNotBlank(customerInfo.getDefaultShippingWarehouse())) {
            dto.setWarehouseId(customerInfo.getDefaultShippingWarehouse());
        }
        if (StringUtils.isBlank(dto.getReceiveAccount()) && StringUtils.isNotBlank(customerInfo.getDefaultReceiveAccount())) {
            dto.setReceiveAccount(customerInfo.getDefaultReceiveAccount());
        }
    }
}
