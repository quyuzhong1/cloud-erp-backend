package com.erp.server.dmp.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zdy
 * @ClassName WebhookController
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("WebHook接收管理")
@RequestMapping("/webhook")
public class WebhookController extends BaseController {
    /**
     * 接收Webhook请求
     * @param payload
     * @param headers
     * @return
     */
    @PostMapping("/receive")
    public ApiResult<String> receiveWebhook(@RequestBody String payload,
                                            @RequestHeader Map<String, String> headers) {
        // 解析请求中的平台标识，进行不同的处理
        String platform = headers.get("X-Platform");  // 假设平台信息通过头部传递
        if (platform == null) {
            return failure("Missing platform header");
        }
        // 根据不同平台的Webhook内容做处理
        switch (platform) {
            case "PlatformA":
//                return handlePlatformA(payload);
            case "PlatformB":
//                return handlePlatformB(payload);
            default:
                return failure("Unknown platform");
        }
    }
}
