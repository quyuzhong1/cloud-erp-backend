package com.erp.server.wms.controller.openapi;

import cn.hutool.core.util.StrUtil;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.TiktokFbtDTO;
import com.erp.server.wms.service.impl.FbtInboundAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/webhook/tiktok")
public class TiktokFbtWebhookController extends BaseController {

    private static final Integer EVENT_TYPE_FBT_INBOUND_STATUS_CHANGE = 21;

    @Resource
    private FbtInboundAsyncService fbtInboundAsyncService;

    @PostMapping
    public ApiResult receiveWebhook(@RequestBody TiktokFbtDTO.WebhookReqDTO reqDTO) {
        if (reqDTO == null) {
            return failure("请求体不能为空");
        }
        if (!EVENT_TYPE_FBT_INBOUND_STATUS_CHANGE.equals(reqDTO.getEventType())) {
            log.warn("TikTok webhook忽略未处理事件, type={}, ttsNotificationId={}",
                    reqDTO.getEventType(), reqDTO.getTtsNotificationId());
            return success();
        }
        if (reqDTO.getData() == null || StrUtil.isBlank(reqDTO.getData().getInboundOrderId())) {
            return failure("inbound_order_id不能为空");
        }
        String inboundOrderId = reqDTO.getData().getInboundOrderId();
        log.info("TikTok FBT webhook接收成功, type={}, sellerOpenId={}, ttsNotificationId={}, orderStatus={}, updateTime={}, inboundOrderId={}",
                reqDTO.getEventType(),
                reqDTO.getSellerOpenId(),
                reqDTO.getTtsNotificationId(),
                reqDTO.getData().getOrderStatus(),
                reqDTO.getData().getUpdateTime(),
                inboundOrderId);
        fbtInboundAsyncService.asyncSyncInboundOrder(inboundOrderId, reqDTO.getSellerOpenId());
        return success();
    }
}
