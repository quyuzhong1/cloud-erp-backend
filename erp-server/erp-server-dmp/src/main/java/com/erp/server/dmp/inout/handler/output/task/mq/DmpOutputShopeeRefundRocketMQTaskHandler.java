package com.erp.server.dmp.inout.handler.output.task.mq;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;

/**
 * Shopee 仅退款 MQ 输出：dmp_so_refund_info/detail → PlatformRefundOrderDTO → OMS。
 * 测试环境暂无 CLOSED 样本，暂用 CANCELLED 验证链路。
 */
@Service
@Scope("prototype")
public class DmpOutputShopeeRefundRocketMQTaskHandler extends DmpOutputPlatformRefundRocketMQTaskHandler {

    private static final String STATUS_REFUND = "CANCELLED";

    @Override
    protected boolean acceptRefund(DmpSoRefundInfoEntity dmpEntity) {
        return dmpEntity != null
                && StringUtils.equalsIgnoreCase(dmpEntity.getPlatformOriginalStatus(), STATUS_REFUND);
    }
}
