package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * B2B三方仓出库状态消费服务
 */
@Slf4j
@Service
public class B2bThirdOutboundConsumerService {

    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;

    public ApiResult<?> handle(String data) {
        PlatformOutboundDTO dto = JSONUtil.toBean(data, PlatformOutboundDTO.class);
        if (Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getReferenceNo())) {
            return ApiResult.success();
        }
        b2bThirdDeliveryService.syncOutboundStatus(dto);
        return ApiResult.success();
    }
}
