package com.erp.server.dmp.inout.handler.output.task.mq;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;

/**
 * Shopee 仅退款 MQ 输出：dmp_so_refund_info/detail → PlatformRefundOrderDTO → OMS。
 * 仅退款完结：平台 status 为 CLOSED。
 */
@Service
@Scope("prototype")
public class DmpOutputShopeeRefundRocketMQTaskHandler extends DmpOutputPlatformRefundRocketMQTaskHandler {

    private static final String STATUS_REFUND_CLOSED = "CLOSED";

    @Override
    protected boolean acceptRefund(DmpSoRefundInfoEntity dmpEntity) {
        return dmpEntity != null
                && StringUtils.equalsIgnoreCase(dmpEntity.getPlatformOriginalStatus(), STATUS_REFUND_CLOSED);
    }
}
