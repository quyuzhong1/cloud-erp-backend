package com.cloud.erp.gateway.component;

import com.alibaba.fastjson.JSON;
import com.cloud.erp.gateway.utils.ServletUtils;
import com.cloud.erp.gateway.web.server.TokenService;
import com.common.business.constant.RedisCacheConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.SignTypeEnum;
import com.erp.model.sys.enums.LogicTypeEnum;
import com.erp.rpc.sys.feign.SysRefereConfigFeign;
import org.redisson.api.RedissonClient;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.utils.ApiSignUtil;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBufferUtils;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
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
@Order(2) // 在AuthGatewayFilter之后执行
public class SignatureVerificationFilter implements GlobalFilter {

    /**
     * 开放API路径
     */
    private static final String OPEN_API_URL = "/open/api/";
    
    /**
     * 需要放行的特定接口路径
     */
    private static final String[] BYPASS_PATHS = {
        "/open/api/getMD5",
        "/open/api/getAES"
    };
    
    /**
     * 签名验证时需要使用空字符串作为 body 的接口路径（如文件上传接口）
     */
    private static final String[] EMPTY_BODY_PATHS = {
        "/open/api/upload/v2"
    };

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TokenService tokenService;

    @Resource
    private SysRefereConfigFeign sysRefereConfigFeign;


    @Value("${spring.profiles.active}")
    private String currentEnvironment;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            String uri = request.getPath().value();

            // 只处理以/open/api开头的请求
            if (!uri.startsWith(OPEN_API_URL)) {
                return chain.filter(exchange);
            }
            
            // 检查是否需要放行的特定接口
            if (isBypassPath(uri)) {
                log.debug("接口 {} 需要放行，跳过签名验证", uri);
                return chain.filter(exchange);
            }

            // 只在 prod 和 uat 环境开启签名认证
//            if (!isSignatureVerificationEnabled()) {
//                log.debug("当前环境 {} 不开启签名认证，URI: {}", currentEnvironment, uri);
//                return chain.filter(exchange);
//            }

            log.info("开始验证签名，URI: {}", uri);

            // 1. 获取请求头
            HttpHeaders headers = request.getHeaders();
            String appId = getAppId(headers);
            String signSessionId = headers.getFirst("Sign-Session-Id");
            String apiSignature = headers.getFirst("API-Signature");

            // 2. 验证必要参数
            if (StringUtils.isBlank(appId)) {
                log.warn("App-Id不能为空，支持的请求头格式：App-Id, appId, AppId, app-id, Referer, referer");
                return unauthorizedResponse(exchange, "应用ID不能为空，", ApiError.ERROR_600.code);
            }
            // 3. 检查应用配置
            AppConfigResult configResult = checkAppConfig(appId);
            if (configResult.isSignatureDisabled()) {
                log.debug("应用 {} 已禁用签名验证，URI: {}", appId, uri);
                return chain.filter(exchange);
            }
            // 4. 检查逻辑类型，如果是OLD则直接放行
            if (configResult.isOldLogic()) {
                log.debug("应用 {} 使用OLD逻辑，直接放行，URI: {}", appId, uri);
                return chain.filter(exchange);
            }

            if (StringUtils.isBlank(apiSignature)) {
                log.warn("API-Signature不能为空");
                return unauthorizedResponse(exchange, ApiError.ERROR_600.msg, ApiError.ERROR_600.code);
            }

            if (StringUtils.isBlank(signSessionId)) {
                log.warn("Sign-Session-Id不能为空");
                return unauthorizedResponse(exchange, ApiError.ERROR_600.msg, ApiError.ERROR_600.code);
            }

            boolean ssoEnabled = configResult.isSsoEnabled();
            String userId = "none"; // 默认值
            LoginUser loginUser = null;

            if (ssoEnabled) {
                // 5. 获取JWT Token并解析用户信息
                String token = headers.getFirst("Authorization");
                if (StringUtils.isBlank(token)) {
                    log.warn("开启单点登录但未提供JWT Token");
                    return unauthorizedResponse(exchange, "未提供JWT Token", ApiError.ERROR_403.code);
                }

                // 移除Bearer前缀
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                loginUser = tokenService.getLoginUser(token);
                if (loginUser == null) {
                    log.warn("JWT Token无效或已过期");
                    return unauthorizedResponse(exchange, "JWT Token无效", ApiError.ERROR_403.code);
                }

                // 从JWT Token中获取用户ID
                userId = loginUser.getUid();
                if (StringUtils.isBlank(userId)) {
                    userId = "none";
                }

                log.info("从JWT Token解析用户ID: {}", userId);
            }

            // 6. 从Redis获取对称密钥
            String redisKey = String.format("sign:session:%s:%s:%s", appId, userId, signSessionId);
            String symmetricKey = null;
            
            try {
                symmetricKey = (String) redissonClient.getBucket(redisKey).get();
            } catch (Exception e) {
                log.error("从Redis获取对称密钥失败，Redis Key: {}, 错误: {}", redisKey, e.getMessage());
                // 如果Redis数据有问题，尝试删除该key
                try {
                    redissonClient.getBucket(redisKey).delete();
                    log.info("已删除有问题的Redis key: {}", redisKey);
                } catch (Exception deleteException) {
                    log.warn("删除有问题的Redis key失败: {}", deleteException.getMessage());
                }
                return unauthorizedResponse(exchange, "会话数据异常，请重新登录", 401);
            }

            if (StringUtils.isBlank(symmetricKey)) {
                log.warn("会话过期，Redis Key: {}", redisKey);
                return unauthorizedResponse(exchange, ApiError.SESSION_EXPIRED.msg, ApiError.SESSION_EXPIRED.code);
            }

            log.info("获取到对称密钥，Redis Key: {}", redisKey);

            // 7. 解析API-Signature头
            String[] parsedSignature = ApiSignUtil.parseSignatureHeader(apiSignature);
            if (parsedSignature == null || parsedSignature.length != 2) {
                log.warn("API-Signature格式错误");
                return unauthorizedResponse(exchange, ApiError.ERROR_400.msg, ApiError.ERROR_400.code);
            }

            String timestampStr = parsedSignature[0];
            String signature = parsedSignature[1];

            // 8. 验证签名
            // 对于文件上传等接口，body 使用空字符串
            String body = isEmptyBodyPath(uri) ? "" : getRequestBody(request);
            boolean isValidSignature = verifySignature(request, signature, timestampStr, symmetricKey, body);
            if (!isValidSignature) {
                log.warn("签名验证失败");
                return unauthorizedResponse(exchange, "签名验证失败", ApiError.ERROR_403.code);
            }

            log.info("签名验证成功，URI: {}", uri);

            // 9. 处理用户信息并添加到请求头
            if (ssoEnabled) {
                // 将用户信息添加到请求头中
                try {
                    String token = headers.getFirst("Authorization");
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    loginUser.setAccessToken(token);
                    request.mutate()
                        .header("App-Id", appId)
                        .header("User-Id", userId)
                        .header("Sign-Session-Id", signSessionId)
                        .header("tokenUserInfo", URLEncoder.encode(JSON.toJSONString(loginUser), "UTF-8"))
                        .build();
                } catch (UnsupportedEncodingException e) {
                    log.error("添加请求头失败", e);
                }
            } else {
                // 未开启单点登录，只添加基础信息
                try {
                    request.mutate()
                        .header("App-Id", appId)
                        .header("User-Id", userId)
                        .header("Sign-Session-Id", signSessionId)
                        .build();
                } catch (Exception e) {
                    log.error("添加请求头失败", e);
                }
            }

        } catch (Exception e) {
            log.error("签名验证异常", e);
            return unauthorizedResponse(exchange, ApiError.ERROR_500.msg, ApiError.ERROR_500.code);
        }

        return chain.filter(exchange);
    }

    /**
     * 获取请求体内容
     * @param request 请求对象
     * @return 请求体字符串
     */
    private String getRequestBody(ServerHttpRequest request) {
        try {
            // 将Flux<DataBuffer>转换为字符串
            return DataBufferUtils.join(request.getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                })
                .defaultIfEmpty("")
                .block(); // 同步阻塞获取结果
                
        } catch (Exception e) {
            log.warn("获取请求体失败", e);
            return "";
        }
    }

    /**
     * 验证签名
     *
     * @param request 请求对象
     * @param signature 签名
     * @param timestampStr 时间戳字符串
     * @param symmetricKey 对称密钥
     * @param body 请求体内容
     * @return 是否验证通过
     */
    private boolean verifySignature(ServerHttpRequest request, String signature, String timestampStr, String symmetricKey, String body) {
        try {
            // 解析时间戳
            long timestamp = Long.parseLong(timestampStr);
            
            // 构建请求参数
            String method = request.getMethod().name();
            String uri = request.getPath().value();

            // 构建查询参数Map
            Map<String, String> queryParams = new HashMap<>();
            request.getQueryParams().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    queryParams.put(key, values.get(0));
                }
            });
            
            // 使用ApiSignUtil验证签名
            // 这里需要确定签名类型，暂时使用HMAC
            String signType = SignTypeEnum.HMAC.getCode(); // 或者从配置中获取
            
            boolean isValid = ApiSignUtil.verifySignature(
                method, uri, body, queryParams, symmetricKey, signType, 
                signature, timestamp
            );
            
            log.info("签名验证结果: {}, 方法: {}, URI: {}, 时间戳: {}", isValid, method, uri, timestamp);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证签名异常", e);
            return false;
        }
    }

    /**
     * 判断是否开启签名认证
     * 只在 prod 和 uat 环境开启
     *
     * @return 是否开启签名认证
     */
    private boolean isSignatureVerificationEnabled() {
        return "prod".equalsIgnoreCase(currentEnvironment) || "uat".equalsIgnoreCase(currentEnvironment);
    }

    /**
     * 应用配置结果
     */
    private static class AppConfigResult {
        private final boolean signatureDisabled;
        private final boolean ssoEnabled;
        private final String logicType;

        public AppConfigResult(boolean signatureDisabled, boolean ssoEnabled, String logicType) {
            this.signatureDisabled = signatureDisabled;
            this.ssoEnabled = ssoEnabled;
            this.logicType = logicType;
        }

        public boolean isSignatureDisabled() {
            return signatureDisabled;
        }

        public boolean isSsoEnabled() {
            return ssoEnabled;
        }

        public String getLogicType() {
            return logicType;
        }

        public boolean isOldLogic() {
            return LogicTypeEnum.OLD.getCode().equalsIgnoreCase(logicType);
        }
    }

    /**
     * 检查应用配置
     *
     * @param appId 应用ID
     * @return 应用配置结果
     */
    private AppConfigResult checkAppConfig(String appId) {
        try {
            // 查询应用配置
            ApiResult<List<SysRefererConfigEntity>> result = sysRefereConfigFeign.getByAppId(appId);

            if (result == null || !result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
                log.warn("未找到应用配置，appId: {}", appId);
                // 默认配置：不禁用签名验证，不开启单点登录，使用NEW逻辑
                return new AppConfigResult(false, false, "NEW");
            }

            List<SysRefererConfigEntity> configList = result.getData();
            boolean signatureDisabled = false;
            boolean ssoEnabled = false;
            String logicType = "NEW"; // 默认使用NEW逻辑

            // 检查配置
            for (SysRefererConfigEntity config : configList) {
                // 检查是否禁用签名验证
                if (config.getSignatureDisabled() != null && config.getSignatureDisabled()) {
                    signatureDisabled = true;
                }
                
                // 检查是否开启单点登录
                if (config.getSsoDisabled() == null || !config.getSsoDisabled()) {
                    ssoEnabled = true;
                }

                // 获取逻辑类型，如果配置了则使用配置的值
                if (StringUtils.isNotBlank(config.getLogicType())) {
                    logicType = config.getLogicType();
                }
            }

            log.info("应用 {} 配置 - 签名验证禁用: {}, 单点登录开启: {}, 逻辑类型: {}", appId, signatureDisabled, ssoEnabled, logicType);
            return new AppConfigResult(signatureDisabled, ssoEnabled, logicType);
        } catch (Exception e) {
            log.error("检查应用配置失败，appId: {}", appId, e);
            // 异常时使用默认配置
            return new AppConfigResult(false, false, "NEW");
        }
    }

    /**
     * 检查是否为需要放行的路径
     * 
     * @param uri 请求URI
     * @return 是否需要放行
     */
    private boolean isBypassPath(String uri) {
        for (String bypassPath : BYPASS_PATHS) {
            if (uri.contains(bypassPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否为需要使用空 body 的接口
     * @param uri 请求URI
     * @return true-需要使用空body，false-使用实际body
     */
    private boolean isEmptyBodyPath(String uri) {
        for (String emptyBodyPath : EMPTY_BODY_PATHS) {
            if (uri.equals(emptyBodyPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取App-Id，兼容多种请求头格式
     * 
     * @param headers 请求头
     * @return App-Id值
     */
    private String getAppId(HttpHeaders headers) {
        // 按优先级顺序检查不同的请求头格式
        String appId = headers.getFirst("App-Id");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }
        
        appId = headers.getFirst("appId");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }
        
        appId = headers.getFirst("AppId");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }
        
        appId = headers.getFirst("app-id");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }
        
        // 兼容旧版本的referer头
        appId = headers.getFirst("Referer");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }
        
        appId = headers.getFirst("referer");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }
        
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg, Integer code) {
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, code);
    }
}
