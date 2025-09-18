package com.cloud.erp.gateway.component;

import com.alibaba.fastjson.JSON;
import com.cloud.erp.gateway.utils.ServletUtils;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.RedisService;
import com.common.core.enums.ApiError;
import com.common.core.utils.ApiSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 签名验证过滤器
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
@Component
@Order(1) // 在AuthGatewayFilter之前执行
public class SignatureVerificationFilter implements GlobalFilter {

    /**
     * 开放API路径
     */
    private static final String OPEN_API_URL = "/open/api/";

    @Resource
    private RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            String uri = request.getPath().value();

            // 只处理以/open/api开头的请求
            if (!uri.startsWith(OPEN_API_URL)) {
                return chain.filter(exchange);
            }

            log.info("开始验证签名，URI: {}", uri);

            // 1. 获取请求头
            HttpHeaders headers = request.getHeaders();
            String appId = headers.getFirst("App-Id");
            String userId = headers.getFirst("User-Id");
            String signSessionId = headers.getFirst("Sign-Session-Id");
            String apiSignature = headers.getFirst("API-Signature");

            // 2. 验证必要参数
            if (StringUtils.isBlank(appId)) {
                log.warn("App-Id不能为空");
                return unauthorizedResponse(exchange, ApiError.ERROR_600.msg, ApiError.ERROR_600.code);
            }

            if (StringUtils.isBlank(apiSignature)) {
                log.warn("API-Signature不能为空");
                return unauthorizedResponse(exchange, ApiError.ERROR_600.msg, ApiError.ERROR_600.code);
            }

            if (StringUtils.isBlank(signSessionId)) {
                log.warn("Sign-Session-Id不能为空");
                return unauthorizedResponse(exchange, ApiError.ERROR_600.msg, ApiError.ERROR_600.code);
            }

            // 3. 如果User-Id为空，使用"none"作为占位符
            if (StringUtils.isBlank(userId)) {
                userId = "none";
            }

            // 4. 从Redis获取对称密钥
            String redisKey = String.format("sign:session:%s:%s:%s", appId, userId, signSessionId);
            String symmetricKey = redisService.getCacheObject(redisKey);

            if (StringUtils.isBlank(symmetricKey)) {
                log.warn("会话过期，Redis Key: {}", redisKey);
                return unauthorizedResponse(exchange, "会话过期", ApiError.ERROR_403.code);
            }

            log.info("获取到对称密钥，Redis Key: {}", redisKey);

            // 5. 解析API-Signature头
            String[] parsedSignature = ApiSignUtil.parseSignatureHeader(apiSignature);
            if (parsedSignature == null || parsedSignature.length != 2) {
                log.warn("API-Signature格式错误");
                return unauthorizedResponse(exchange, ApiError.ERROR_400.msg, ApiError.ERROR_400.code);
            }

            String timestampStr = parsedSignature[0];
            String signature = parsedSignature[1];

            // 6. 验证签名
            boolean isValidSignature = verifySignature(request, signature, timestampStr, symmetricKey);
            if (!isValidSignature) {
                log.warn("签名验证失败");
                return unauthorizedResponse(exchange, "签名验证失败", ApiError.ERROR_403.code);
            }

            log.info("签名验证成功，URI: {}", uri);

            // 7. 将用户信息添加到请求头中
            try {
                // 这里可以根据需要添加用户信息到请求头
                request.mutate()
                    .header("App-Id", appId)
                    .header("User-Id", userId)
                    .header("Sign-Session-Id", signSessionId)
                    .build();
            } catch (Exception e) {
                log.error("添加请求头失败", e);
            }

        } catch (Exception e) {
            log.error("签名验证异常", e);
            return unauthorizedResponse(exchange, ApiError.ERROR_500.msg, ApiError.ERROR_500.code);
        }

        return chain.filter(exchange);
    }

    /**
     * 验证签名
     *
     * @param request 请求对象
     * @param signature 签名
     * @param timestampStr 时间戳字符串
     * @param symmetricKey 对称密钥
     * @return 是否验证通过
     */
    private boolean verifySignature(ServerHttpRequest request, String signature, String timestampStr, String symmetricKey) {
        try {
            // 解析时间戳
            long timestamp = Long.parseLong(timestampStr);
            
            // 构建请求参数
            String method = request.getMethod().name();
            String uri = request.getPath().value();
            String body = ""; // 这里需要根据实际情况获取请求体
            
            // 构建查询参数Map
            Map<String, String> queryParams = new HashMap<>();
            request.getQueryParams().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    queryParams.put(key, values.get(0));
                }
            });
            
            // 使用ApiSignUtil验证签名
            // 这里需要确定签名类型，暂时使用AES
            String signType = "AES"; // 或者从配置中获取
            
            boolean isValid = ApiSignUtil.verifySignature(
                method, uri, body, queryParams, symmetricKey, signType, 
                signature, timestamp
            );
            
            log.debug("签名验证结果: {}, 方法: {}, URI: {}, 时间戳: {}", isValid, method, uri, timestamp);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证签名异常", e);
            return false;
        }
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg, Integer code) {
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, code);
    }
}
