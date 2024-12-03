package com.erp.server.dmp.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.factory.WebhookHandlerFactory;
import com.erp.server.dmp.handler.WebhookHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
    // 预先约定的Secret
    private static final String SECRET_KEY = "your_secret_key";
    // 允许的时间偏差（秒）
    private static final long MAX_AGE = 5 * 60; // 5 minutes
    private final WebhookHandlerFactory webhookHandlerFactory;

    public WebhookController(WebhookHandlerFactory webhookHandlerFactory) {
        this.webhookHandlerFactory = webhookHandlerFactory;
    }

    /**
     * 接收Webhook请求
     * @param payload
     * @param headers
     * @return
     */
    @PostMapping("/receive")
    public ApiResult<String> receiveWebhook(@RequestBody String payload,
                                            @RequestHeader Map<String, String> headers) {
        log.info("========接收到webhook接口请求=======start");
        // 解析请求中的平台标识，进行不同的处理
        String platform = getPlatform(headers,payload);
        //基础校验
        baseVerify(payload,headers);
        // 根据不同平台的Webhook内容做处理
        switch (platform) {
            case "track123":
                WebhookHandler handler = webhookHandlerFactory.getHandler(platform);
                //安全校验
//                handler.verify(payload);
                //业务处理
                handler.process(payload);
                log.info("========接收到webhook接口请求=======end");
                return success();
            default:
                log.info("========接收到webhook接口请求=======end");
                return failure("Unknown platform");
        }
    }

    /**
     * 基础请求校验
     *
     * @param payload
     * @param headers
     */
    private void baseVerify(String payload, Map<String, String> headers) {
        String signature = headers.get("X-Hub-Signature");
        String timestamp = headers.get("X-Hub-Timestamp");
        // 校验时间戳
        if (CharSequenceUtil.isNotBlank(timestamp) && Math.abs((System.currentTimeMillis() - Long.parseLong(timestamp))/1000) > MAX_AGE) {
            throw new ServiceException("Request is too old or timestamp is missing");
        }

        // 校验签名
        if (CharSequenceUtil.isNotBlank(signature) && !verifySignature(payload, signature)) {
            throw new ServiceException("Invalid signature");
        }
    }

    private String getPlatform(Map<String, String> headers, String payload) {
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
        if (platform == null) {
            return "track123";
        }

        return "track123";
    }
    private boolean verifySignature(String data, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);

            byte[] bytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = Base64.getEncoder().encodeToString(bytes);

            // 注意：这里假设传入的签名是"sha256="前缀后的实际Base64编码值
            if (signature.startsWith("sha256=")) {
                signature = signature.substring(7);
            }

            return generatedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }
}
