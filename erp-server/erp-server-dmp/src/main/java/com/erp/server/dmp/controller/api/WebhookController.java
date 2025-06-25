package com.erp.server.dmp.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.dmp.enums.WebhookServiceEnum;
import com.erp.server.dmp.factory.WebhookHandlerFactory;
import com.erp.server.dmp.handler.WebhookHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

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
    // 预先约定的Secret
    private static final String SECRET_KEY = "your_secret_key";
    // 允许的时间偏差（秒）
    private static final long MAX_AGE = 5 * 60L; // 5 minutes
    private final WebhookHandlerFactory webhookHandlerFactory;

    public WebhookController(WebhookHandlerFactory webhookHandlerFactory) {
        this.webhookHandlerFactory = webhookHandlerFactory;
    }

    /**
     * 接收Webhook请求
     * @param serviceFlag 服务名称
     * @param data 传递数据
     * @param headers 请求头
     * @return
     */
    @PostMapping("/receive/{serviceFlag}")
    public String receiveWebhook(@PathVariable("serviceFlag") String serviceFlag,
                                 @RequestBody String data,
                                 @RequestHeader Map<String, String> headers) {
        log.info("========接收到webhook接口请求=======start");
        log.info("receiveWebhook:serviceFlag:{},data:{},headers:{}",serviceFlag,data,headers);
        // 解析请求中的服务标识，进行不同的处理
        String service = getService(serviceFlag, headers, data);
        // 根据不同平台的Webhook内容做处理
        WebhookHandler handler = webhookHandlerFactory.getHandler(service);
        //安全校验
        handler.verify(data,headers,serviceFlag);
        //业务处理
        String result = handler.process(data,headers,serviceFlag);
        log.info("========接收到webhook接口请求=======end");
        return result;
    }

    private String getService(String serviceFlag, Map<String, String> headers, String data) {
        WebhookServiceEnum serviceEnum = WebhookServiceEnum.getByName(serviceFlag);
        if (Objects.nonNull(serviceEnum)){
            return serviceEnum.getCode();
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return "";
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取请求的完整URL
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }
        String platform = headers.get("X-Platform");  // 假设平台信息通过头部传递
        return "";
    }
}
