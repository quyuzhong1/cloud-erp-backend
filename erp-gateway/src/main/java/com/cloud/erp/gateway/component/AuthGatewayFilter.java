package com.cloud.erp.gateway.component;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.cloud.erp.gateway.config.JwtProperties;
import com.cloud.erp.gateway.context.GatewayContext;
import com.cloud.erp.gateway.utils.GatewayLocaleUtils;
import com.cloud.erp.gateway.utils.IpRateLimitUtil;
import com.cloud.erp.gateway.utils.ServletUtils;
import com.cloud.erp.gateway.web.server.TokenService;
import com.common.business.constant.AuthPassPath;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.constant.TokenConstants;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.sys.constants.SysApiTokenConstants;
import com.erp.model.sys.dto.SysApiTokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import com.erp.rpc.sys.feign.SysApiTokenFeign;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @Classname AuthGatewayFilter
 * @Date 2022-07-11 11:38
 * @Created by yl
 */
@Slf4j
@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {
    /**
     * Feign资源前缀
     */
    private static final String FEIGN_URL = "/feign/";
    /**
     * 移动端
     */
    private static final String APP_URL = "/app/";

    /**
     * 开放API
     */
    private static final String OPEN_API_URL = "/open/api/";

    /**
     * 单点登录
     */
    private static final String SSO_URL = "/sso";

    private static final String KEY_REGISTER_URL = "/key/register";

    private static final String API_SYS_PREFIX = "/api/sys";

    private static final String SYS_SERVICE_PREFIX = "/sys";

    private static final String PERSONAL_CENTER_API_TOKEN_PATH = "/personalCenter/apiToken";

    private static final String API_TOKEN_WHITELIST_PATH = "/apiTokenWhitelist";

    private static final String API_SYS_EVENT_TRACKING_PATH = "/api/sys" + AuthPassPath.EVENT_TRACKING_PATH;

    private static final String[] API_TOKEN_MANAGEMENT_DENY_PATHS = {
            API_SYS_PREFIX + PERSONAL_CENTER_API_TOKEN_PATH,
            API_SYS_PREFIX + API_TOKEN_WHITELIST_PATH,
            SYS_SERVICE_PREFIX + PERSONAL_CENTER_API_TOKEN_PATH,
            SYS_SERVICE_PREFIX + API_TOKEN_WHITELIST_PATH,
            PERSONAL_CENTER_API_TOKEN_PATH,
            API_TOKEN_WHITELIST_PATH
    };

    /**
     * 成功结果缓存很短，降低高频合法调用的 Feign 压力，同时控制 token 删除后的最大延迟。
     */
    private static final int API_TOKEN_VALIDATE_SUCCESS_CACHE_SECONDS = 5;

    /**
     * 无效 token 做更短的负缓存，拦截重复探测，避免随机 token 攻击长时间占用 Redis。
     */
    private static final int API_TOKEN_VALIDATE_INVALID_CACHE_SECONDS = 3;

    /**
     * token 有效但接口未进白名单时缓存 5 秒，兼顾防重复 Feign 和后台新增白名单后的生效速度。
     */
    private static final int API_TOKEN_VALIDATE_FORBIDDEN_CACHE_SECONDS = 5;

    /**
     * 32 字节随机数做 Base64URL 无 padding 后固定为 43 位。
     */
    private static final int API_TOKEN_RANDOM_LENGTH = 43;

    private static final int API_TOKEN_TOTAL_LENGTH = SysApiTokenConstants.TOKEN_PREFIX.length() + API_TOKEN_RANDOM_LENGTH;

    private static final Pattern API_TOKEN_PATTERN = Pattern.compile("^" + Pattern.quote(SysApiTokenConstants.TOKEN_PREFIX) + "[A-Za-z0-9_-]{43}$");

    static final String MCP_INVOCATION_ID_HEADER = "X-MCP-Invocation-Id";

    private static final Pattern MCP_INVOCATION_ID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    private static final Pattern UUID_PATH_SEGMENT_PATTERN = Pattern.compile(
            "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private static final Pattern NUMERIC_PATH_SEGMENT_PATTERN = Pattern.compile("^[0-9]+$");

    private static final Pattern SAFE_PATH_SEGMENT_PATTERN = Pattern.compile("^[A-Za-z0-9._~-]+$");

    private static final Pattern LONG_ID_PATH_SEGMENT_PATTERN = Pattern.compile(
            "^(?=.{24,}$)(?=.*[0-9])[A-Za-z0-9_-]+$");

    private static final String UNKNOWN_IP = "unknown";

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private static final String X_REAL_IP = "X-Real-IP";

    private static final int IPV4_BIT_LENGTH = 32;

    @Resource
    private TokenService tokenService;
    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private IpRateLimitUtil ipRateLimitUtil;

    @Resource
    private GatewayLocaleUtils localeUtils;

    @Resource
    private SysApiTokenFeign sysApiTokenFeign;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 只有直接连接方在这些网段内，才信任代理写入的 X-Forwarded-For/X-Real-IP。
     * 代理层需要清洗并重写转发头，避免客户端自带伪造头透传到网关。
     */
    @Value("${gateway.client-ip.trusted-proxy-cidrs:}")
    private String trustedProxyCidrs;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {  //获取请求
            List<String> invocationHeaderValues = exchange.getRequest().getHeaders().get(MCP_INVOCATION_ID_HEADER);
            List<String> incomingMcpInvocationIds = invocationHeaderValues == null
                    ? null : new ArrayList<>(invocationHeaderValues);
            ServerHttpRequest request = stripApiTokenInternalHeaders(exchange.getRequest());
            exchange = exchange.mutate().request(request).build();
            // 获取请求URL
            String uri = request.getPath().value();
            //判断是否有feign
            if (uri.contains(FEIGN_URL)) {
                //文件头使用JSON格式
                return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_FORBIDDEN, exchange.getRequest()), ApiError.HTTP_FORBIDDEN.getCode());
            }
            Mono<Void> apiTokenAuthResult = tryApiTokenAuth(exchange, chain, request, uri, incomingMcpInvocationIds);
            if (apiTokenAuthResult != null) {
                return apiTokenAuthResult;
            }
            if (uri.contains(SSO_URL)||uri.contains(KEY_REGISTER_URL)){
                Mono<Void> rateLimitResult = checkOpenApiRateLimit(exchange, request, uri);
                if (rateLimitResult != null) {
                    return rateLimitResult;
                }
                return chain.filter(exchange);
            }
            //判断是否是开放API 如果是 进行IP防护后放行
            if (uri.contains(OPEN_API_URL)) {
                Mono<Void> rateLimitResult = checkOpenApiRateLimit(exchange, request, uri);
                if (rateLimitResult != null) {
                    return rateLimitResult;
                }
                return chain.filter(exchange);
            }


            //判断请求路径在不在拦截名单中，在直接放行
            Boolean flag = false;
            List<String> pathList = Arrays.asList(AuthPassPath.PASS_PATH_LIST.split(";"));
            for (String authPath : pathList) {
                if (uri.indexOf(authPath) != -1) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                return chain.filter(exchange);
            }
            // 埋点路径解析
            if (isEventTrackingPath(uri)) {
                // 解析请求参数token用户
                parseFormDataToken(exchange, request);
                return chain.filter(exchange);
            }

            // 检查URL是否以/open/api开头
            if (uri.startsWith(OPEN_API_URL)) {
                // URL以/open/api开头，由SignatureVerificationFilter处理签名验证
                return chain.filter(exchange);
            } else {
                // URL不是以/open/api开头，进行JWT Token验证
                HttpHeaders headers = request.getHeaders();
                String token = headers.getFirst(TokenConstants.AUTHENTICATION);
                if (StringUtils.isBlank(token)) {
                    // 响应中放入返回的状态吗, 没有权限访问
                    Mono<Void> mono = unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
                    return mono;
                }
                //解析token
                LoginUser loginUser = tokenService.getLoginUser(token);
                if (Objects.isNull(loginUser)) {
                    return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
                }

                // 检查JWT Token是否包含pathList权限
                String[] jwtPathList = com.erp.model.sys.utils.JwtUtils.getPathList(token, jwtProperties.getSecret());
                if (jwtPathList.length > 0) {
                    // JWT Token包含pathList权限，检查当前路径是否有权限
                    boolean hasPermission = checkPathPermission(uri, jwtPathList);
                    if (!hasPermission) {
                        log.warn("接口无权限，URI: {}, 用户权限: {}", uri, Arrays.toString(jwtPathList));
                        return unauthorizedResponse(exchange, "接口无权限", ApiError.HTTP_FORBIDDEN.getCode());
                    }
                }

                loginUser.setAccessToken(token);
                // 只传后端需要的基础字段，不传 permissionList/menuList，避免 header 过大导致 HTTP 解析异常
                String tokenUserInfo = LoginUser.simpleLoginUser(loginUser);
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("tokenUserInfo", tokenUserInfo)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return 0;
    }


    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg, Integer code) {
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, code);
    }

    private boolean isEventTrackingPath(String uri) {
        return AuthPassPath.EVENT_TRACKING_PATH.equals(uri) || API_SYS_EVENT_TRACKING_PATH.equals(uri);
    }

    private Mono<Void> tryApiTokenAuth(ServerWebExchange exchange, GatewayFilterChain chain,
                                       ServerHttpRequest request, String uri, List<String> incomingMcpInvocationIds) {
        String apiToken = resolveApiTokenFromAuthorization(request.getHeaders().getFirst(TokenConstants.AUTHENTICATION));
        if (StringUtils.isBlank(apiToken)) {
            return null;
        }
        String invocationId = resolveMcpInvocationId(incomingMcpInvocationIds);
        setMcpInvocationIdHeader(exchange.getResponse().getHeaders(), invocationId);
        long startNanos = System.nanoTime();
        logMcpGatewayEvent(exchange, "mcp_gateway_ingress", invocationId, null, null);
        if (isApiTokenManagementPath(uri)) {
            return withMcpGatewayEgressAudit(exchange,
                    unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.AUTH_API_TOKEN_MANAGEMENT_PATH_FORBIDDEN, exchange.getRequest()), ApiError.HTTP_FORBIDDEN.getCode()),
                    invocationId, startNanos);
        }
        if (!isApiTokenFormatValid(apiToken)) {
            return withMcpGatewayEgressAudit(exchange,
                    unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode()),
                    invocationId, startNanos);
        }

        String tokenHash = sha256Hex(apiToken);
        Mono<Void> result = Mono.fromCallable(() -> authenticateApiToken(exchange, request, uri, tokenHash, invocationId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(decision -> {
                    if (decision.authenticated) {
                        return stripApiTokenRestrictedFields(decision.authenticatedExchange, chain);
                    }
                    return unauthorizedResponse(exchange, decision.msg, decision.code);
                })
                .onErrorResume(e -> {
                    log.error("API Token校验失败，URI: {}", uri, e);
                    return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
                });
        return withMcpGatewayEgressAudit(exchange, result, invocationId, startNanos);
    }

    static boolean isValidMcpInvocationId(String invocationId) {
        return invocationId != null && MCP_INVOCATION_ID_PATTERN.matcher(invocationId).matches();
    }

    static String resolveMcpInvocationId(List<String> headerValues) {
        if (headerValues != null && headerValues.size() == 1 && isValidMcpInvocationId(headerValues.get(0))) {
            return headerValues.get(0);
        }
        return UUID.randomUUID().toString();
    }

    static void setMcpInvocationIdHeader(HttpHeaders headers, String invocationId) {
        headers.remove(MCP_INVOCATION_ID_HEADER);
        headers.set(MCP_INVOCATION_ID_HEADER, invocationId);
    }

    static String sanitizeMcpAuditPath(String path) {
        if (StringUtils.isBlank(path)) {
            return "/";
        }
        String[] segments = path.split("/", -1);
        for (int index = 0; index < segments.length; index++) {
            String segment = segments[index];
            if (StringUtils.isBlank(segment)) {
                continue;
            }
            if (!SAFE_PATH_SEGMENT_PATTERN.matcher(segment).matches()
                    || NUMERIC_PATH_SEGMENT_PATTERN.matcher(segment).matches()
                    || UUID_PATH_SEGMENT_PATTERN.matcher(segment).matches()
                    || LONG_ID_PATH_SEGMENT_PATTERN.matcher(segment).matches()) {
                segments[index] = ":id";
            }
        }
        return String.join("/", segments);
    }

    private Mono<Void> withMcpGatewayEgressAudit(ServerWebExchange exchange, Mono<Void> result,
                                                  String invocationId, long startNanos) {
        return result.doFinally(signalType -> {
            Integer responseStatus = exchange.getResponse().getRawStatusCode();
            int status = responseStatus != null ? responseStatus : defaultStatus(signalType);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            logMcpGatewayEvent(exchange, "mcp_gateway_egress", invocationId, status, durationMs);
        });
    }

    private static int defaultStatus(SignalType signalType) {
        if (SignalType.ON_ERROR.equals(signalType)) {
            return 500;
        }
        if (SignalType.CANCEL.equals(signalType)) {
            return 499;
        }
        return 200;
    }

    /**
     * MCP 关联日志只记录固定的请求元数据，不记录 Token、Header、查询参数或请求体。
     */
    private void logMcpGatewayEvent(ServerWebExchange exchange, String event, String invocationId,
                                    Integer status, Long durationMs) {
        JSONObject audit = new JSONObject();
        audit.set("event", event);
        audit.set("invocationId", invocationId);
        audit.set("method", exchange.getRequest().getMethodValue());
        audit.set("path", sanitizeMcpAuditPath(exchange.getRequest().getPath().value()));
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        audit.set("routeId", route == null ? "unknown" : route.getId());
        if (status != null) {
            audit.set("status", status);
        }
        if (durationMs != null) {
            audit.set("durationMs", durationMs);
        }
        log.info("{}", JSONUtil.toJsonStr(audit));
    }

    /**
     * API Token 调用不允许客户端控制后端 SQL 片段。普通 JWT 请求不会进入此方法，
     * advanceQueryDTOList 等结构化条件会被原样保留。
     */
    Mono<Void> stripApiTokenRestrictedFields(ServerWebExchange exchange, GatewayFilterChain chain) {
        MediaType contentType = exchange.getRequest().getHeaders().getContentType();
        if (HttpMethod.GET.equals(exchange.getRequest().getMethod()) || !isJsonContentType(contentType)) {
            return chain.filter(exchange);
        }
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return bytes;
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .defaultIfEmpty(new byte[0])
                .flatMap(originalBytes -> {
                    if (originalBytes.length == 0) {
                        return chain.filter(exchange);
                    }
                    byte[] forwardedBytes = originalBytes;
                    try {
                        Object body = JSONUtil.parse(new String(originalBytes, StandardCharsets.UTF_8));
                        if (removeApiTokenRestrictedFieldsRecursively(body)) {
                            forwardedBytes = JSONUtil.toJsonStr(body).getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (Exception e) {
                        log.warn("API Token请求体不是有效JSON，已拒绝，URI: {}", exchange.getRequest().getPath().value());
                        return unauthorizedResponse(exchange,
                                localeUtils.getMessage(ApiError.HTTP_BAD_REQUEST, exchange.getRequest()),
                                ApiError.HTTP_BAD_REQUEST.getCode());
                    }
                    return forwardBody(exchange, chain, forwardedBytes);
                });
    }

    static boolean isJsonContentType(MediaType contentType) {
        if (contentType == null || !"application".equalsIgnoreCase(contentType.getType())) {
            return false;
        }
        String subtype = contentType.getSubtype();
        return "json".equalsIgnoreCase(subtype)
                || subtype.toLowerCase(Locale.ROOT).endsWith("+json");
    }

    static boolean removeApiTokenRestrictedFieldsRecursively(Object node) {
        boolean changed = false;
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            for (String key : new ArrayList<>(object.keySet())) {
                if (isApiTokenRestrictedField(key)) {
                    object.remove(key);
                    changed = true;
                } else {
                    changed = removeApiTokenRestrictedFieldsRecursively(object.get(key)) || changed;
                }
            }
        } else if (node instanceof JSONArray) {
            for (Object item : (JSONArray) node) {
                changed = removeApiTokenRestrictedFieldsRecursively(item) || changed;
            }
        }
        return changed;
    }

    private static boolean isApiTokenRestrictedField(String key) {
        return "sqlMap".equalsIgnoreCase(key)
                || "permissionSql".equalsIgnoreCase(key)
                || "dataScope".equalsIgnoreCase(key);
    }

    private Mono<Void> forwardBody(ServerWebExchange exchange, GatewayFilterChain chain, byte[] bodyBytes) {
        GatewayContext<?> gatewayContext = exchange.getAttribute(GatewayContext.CACHE_GATEWAY_CONTEXT);
        if (gatewayContext != null) {
            gatewayContext.setRequestBody(new String(bodyBytes, StandardCharsets.UTF_8));
        }
        ServerHttpRequestDecorator request = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.remove(HttpHeaders.CONTENT_LENGTH);
                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                headers.setContentLength(bodyBytes.length);
                return headers;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(bodyBytes)));
            }
        };
        return chain.filter(exchange.mutate().request(request).build());
    }

    private ApiTokenAuthDecision authenticateApiToken(ServerWebExchange exchange, ServerHttpRequest request,
                                                     String uri, String tokenHash, String invocationId) {
        // API Token 是入口级能力，限流、Redis 缓存和 Feign 都是阻塞调用，统一隔离到 boundedElastic。
        ApiTokenAuthDecision rateLimitDecision = checkApiTokenRateLimit(exchange, request, uri);
        if (rateLimitDecision != null) {
            return rateLimitDecision;
        }
        if (ipRateLimitUtil.isApiTokenFailureBlocked(tokenHash)) {
            log.warn("API Token失败次数过多，已临时封禁，URI: {}", uri);
            return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
        }

        SysApiTokenDTO.ValidateReqDTO dto = new SysApiTokenDTO.ValidateReqDTO();
        dto.setTokenHash(tokenHash);
        dto.setRequestPath(uri);

        try {
            SysApiTokenDTO.ValidateRespDTO validateResp = getCachedApiTokenValidate(tokenHash, uri);
            if (validateResp == null) {
                ApiResult<SysApiTokenDTO.ValidateRespDTO> result = sysApiTokenFeign.validate(dto);
                validateResp = result == null ? null : result.getData();
                // Feign/系统异常不写缓存，只缓存 sys 明确返回的业务校验结果。
                if (result == null || !result.isSuccess() || validateResp == null) {
                    return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
                }
                cacheApiTokenValidate(tokenHash, uri, validateResp);
            }
            if (!Boolean.TRUE.equals(validateResp.getTokenValid())) {
                recordApiTokenFailure(tokenHash, uri, "invalid");
                return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
            }
            if (!Boolean.TRUE.equals(validateResp.getPathAllowed())) {
                log.warn("API Token接口未配置白名单，URI: {}, tokenId: {}", uri, validateResp.getTokenId());
                recordApiTokenFailure(tokenHash, uri, "forbidden");
                return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.AUTH_API_TOKEN_PATH_NOT_IN_WHITELIST, exchange.getRequest()), ApiError.HTTP_FORBIDDEN.getCode());
            }

            LoginUser loginUser = buildLoginUser(validateResp);
            String tokenUserInfo = LoginUser.simpleLoginUser(loginUser);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove(TokenConstants.AUTHENTICATION);
                        headers.remove(SysApiTokenConstants.INTERNAL_AUTH_HEADER);
                        headers.remove(SysApiTokenConstants.INTERNAL_TOKEN_ID_HEADER);
                        setMcpInvocationIdHeader(headers, invocationId);
                    })
                    .header("tokenUserInfo", tokenUserInfo)
                    .header(SysApiTokenConstants.INTERNAL_AUTH_HEADER, SysApiTokenConstants.INTERNAL_AUTH_VALUE)
                    .header(SysApiTokenConstants.INTERNAL_TOKEN_ID_HEADER, validateResp.getTokenId())
                    .build();
            ServerWebExchange authenticatedExchange = exchange.mutate().request(mutatedRequest).build();
            // 签名过滤器只信任 exchange 内部属性，不信任客户端可伪造的同名请求头。
            authenticatedExchange.getAttributes().put(SysApiTokenConstants.INTERNAL_AUTH_ATTRIBUTE, Boolean.TRUE);
            return ApiTokenAuthDecision.success(authenticatedExchange);
        } catch (UnsupportedEncodingException e) {
            log.error("API Token用户信息编码失败，URI: {}", uri, e);
            return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_UNKNOWN, exchange.getRequest()), ApiError.HTTP_UNKNOWN.getCode());
        } catch (Exception e) {
            log.error("API Token校验失败，URI: {}", uri, e);
            return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
        }
    }

    private ApiTokenAuthDecision checkApiTokenRateLimit(ServerWebExchange exchange, ServerHttpRequest request, String uri) {
        ApiTokenAuthDecision ipRateLimitDecision = checkOpenApiRateLimitDecision(exchange, request, uri);
        if (ipRateLimitDecision != null) {
            return ipRateLimitDecision;
        }
        if (!ipRateLimitUtil.isApiTokenGlobalAllowed()) {
            log.warn("API Token全局访问频率过高，URI: {}", uri);
            return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_TOO_MANY_REQUESTS, exchange.getRequest()), ApiError.HTTP_TOO_MANY_REQUESTS.getCode());
        }
        if (!ipRateLimitUtil.isApiTokenPathAllowed(sha256Hex(uri))) {
            log.warn("API Token路径访问频率过高，URI: {}", uri);
            return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_TOO_MANY_REQUESTS, exchange.getRequest()), ApiError.HTTP_TOO_MANY_REQUESTS.getCode());
        }
        return null;
    }

    private Mono<Void> checkOpenApiRateLimit(ServerWebExchange exchange, ServerHttpRequest request, String uri) {
        ApiTokenAuthDecision decision = checkOpenApiRateLimitDecision(exchange, request, uri);
        return decision == null ? null : unauthorizedResponse(exchange, decision.msg, decision.code);
    }

    private ApiTokenAuthDecision checkOpenApiRateLimitDecision(ServerWebExchange exchange, ServerHttpRequest request, String uri) {
        String remoteIp = getRemoteIp(request);
        boolean trustedProxy = isTrustedProxy(remoteIp);
        String xForwardedFor = request.getHeaders().getFirst(X_FORWARDED_FOR);
        String xRealIp = request.getHeaders().getFirst(X_REAL_IP);
        String clientIp = getClientIp(request, remoteIp);
        log.info("开放接口限流IP解析，clientIp: {}, remoteIp: {}, trustedProxy: {}, xForwardedFor: {}, xRealIp: {}, URI: {}",
                clientIp,
                remoteIp,
                trustedProxy,
                xForwardedFor,
                xRealIp,
                uri);
        if (!trustedProxy && (StringUtils.isNotBlank(xForwardedFor) || StringUtils.isNotBlank(xRealIp))) {
            log.warn("开放接口忽略代理IP头，remoteIp未配置为可信代理，clientIp按remoteIp限流，remoteIp: {}, trustedProxyCidrs: {}, xForwardedFor: {}, xRealIp: {}, URI: {}",
                    remoteIp,
                    trustedProxyCidrs,
                    xForwardedFor,
                    xRealIp,
                    uri);
        }
        if (!ipRateLimitUtil.isOpenApiAllowed(clientIp)) {
            log.warn("开放接口访问频率过高或限流组件不可用，IP: {}, URI: {}", clientIp, uri);
            return ApiTokenAuthDecision.failure(localeUtils.getMessage(ApiError.HTTP_TOO_MANY_REQUESTS, exchange.getRequest()), ApiError.HTTP_TOO_MANY_REQUESTS.getCode());
        }
        return null;
    }

    private static class ApiTokenAuthDecision {
        private final boolean authenticated;
        private final ServerWebExchange authenticatedExchange;
        private final String msg;
        private final Integer code;

        private ApiTokenAuthDecision(boolean authenticated, ServerWebExchange authenticatedExchange, String msg, Integer code) {
            this.authenticated = authenticated;
            this.authenticatedExchange = authenticatedExchange;
            this.msg = msg;
            this.code = code;
        }

        private static ApiTokenAuthDecision success(ServerWebExchange authenticatedExchange) {
            return new ApiTokenAuthDecision(true, authenticatedExchange, null, null);
        }

        private static ApiTokenAuthDecision failure(String msg, Integer code) {
            return new ApiTokenAuthDecision(false, null, msg, code);
        }
    }

    private ServerHttpRequest stripApiTokenInternalHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(SysApiTokenConstants.INTERNAL_AUTH_HEADER);
                    headers.remove(SysApiTokenConstants.INTERNAL_TOKEN_ID_HEADER);
                    headers.remove(MCP_INVOCATION_ID_HEADER);
                })
                .build();
    }

    private String resolveApiTokenFromAuthorization(String authorization) {
        if (StringUtils.isBlank(authorization) || !authorization.startsWith(TokenConstants.PREFIX)) {
            return null;
        }
        String credential = authorization.substring(TokenConstants.PREFIX.length()).trim();
        // 只有明确使用个人访问令牌前缀的 Bearer 凭证才进入 API Token 分支，普通 ERP JWT 继续走原登录鉴权。
        return credential.startsWith(SysApiTokenConstants.TOKEN_PREFIX) ? credential : null;
    }

    private boolean isApiTokenFormatValid(String apiToken) {
        return apiToken.length() == API_TOKEN_TOTAL_LENGTH && API_TOKEN_PATTERN.matcher(apiToken).matches();
    }

    private boolean isApiTokenManagementPath(String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        if (uri.contains(FEIGN_URL)) {
            return true;
        }
        for (String denyPath : API_TOKEN_MANAGEMENT_DENY_PATHS) {
            if (isSamePathOrChild(uri, denyPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSamePathOrChild(String uri, String basePath) {
        String normalizedUri = StringUtils.removeEnd(uri, "/");
        String normalizedBasePath = StringUtils.removeEnd(basePath, "/");
        return normalizedUri.equals(normalizedBasePath) || normalizedUri.startsWith(normalizedBasePath + "/");
    }

    private LoginUser buildLoginUser(SysApiTokenDTO.ValidateRespDTO validateResp) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUid(validateResp.getUserId());
        loginUser.setUserName(validateResp.getUserName());
        loginUser.setRealName(validateResp.getRealName());
        loginUser.setMobile(validateResp.getMobile());
        loginUser.setUserAccount(validateResp.getUserAccount());
        loginUser.setIsSupper(validateResp.getSuperAdmin());
        return loginUser;
    }

    private SysApiTokenDTO.ValidateRespDTO getCachedApiTokenValidate(String tokenHash, String uri) {
        try {
            return (SysApiTokenDTO.ValidateRespDTO) redissonClient.getBucket(buildApiTokenValidateCacheKey(tokenHash, uri)).get();
        } catch (Exception e) {
            log.warn("读取API Token校验缓存失败，URI: {}", uri, e);
            return null;
        }
    }

    private void cacheApiTokenValidate(String tokenHash, String uri, SysApiTokenDTO.ValidateRespDTO validateResp) {
        int cacheSeconds = getApiTokenValidateCacheSeconds(validateResp);
        if (cacheSeconds <= 0) {
            return;
        }
        try {
            redissonClient.getBucket(buildApiTokenValidateCacheKey(tokenHash, uri))
                    .set(validateResp, cacheSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入API Token校验缓存失败，URI: {}", uri, e);
        }
    }

    private int getApiTokenValidateCacheSeconds(SysApiTokenDTO.ValidateRespDTO validateResp) {
        if (validateResp == null) {
            return 0;
        }
        if (!Boolean.TRUE.equals(validateResp.getTokenValid())) {
            // 已知 token 的失效状态可能因延期、启用用户等后台操作恢复，避免缓存导致恢复后短暂误拒。
            if (StringUtils.isNotBlank(validateResp.getTokenId())) {
                return 0;
            }
            return API_TOKEN_VALIDATE_INVALID_CACHE_SECONDS;
        }
        if (!Boolean.TRUE.equals(validateResp.getPathAllowed())) {
            return capCacheSecondsByTokenExpiry(API_TOKEN_VALIDATE_FORBIDDEN_CACHE_SECONDS, validateResp.getExpiresTime());
        }
        return capCacheSecondsByTokenExpiry(API_TOKEN_VALIDATE_SUCCESS_CACHE_SECONDS, validateResp.getExpiresTime());
    }

    private int capCacheSecondsByTokenExpiry(int cacheSeconds, LocalDateTime expiresTime) {
        if (expiresTime == null) {
            return cacheSeconds;
        }
        long secondsUntilExpire = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiresTime);
        if (secondsUntilExpire <= 0) {
            return 0;
        }
        return (int) Math.min(cacheSeconds, secondsUntilExpire);
    }

    private void recordApiTokenFailure(String tokenHash, String uri, String reason) {
        if (!ipRateLimitUtil.recordApiTokenFailure(tokenHash)) {
            log.warn("API Token失败次数达到封禁阈值，URI: {}, reason: {}", uri, reason);
        }
    }

    private String buildApiTokenValidateCacheKey(String tokenHash, String uri) {
        // 缓存粒度包含请求路径，因为同一个 token 可能只被白名单允许访问部分接口。
        return RedisCacheConstants.GATEWAY_API_TOKEN_VALIDATE.replace("{}", sha256Hex(tokenHash + ":" + uri));
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                sb.append(String.format("%02x", item));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成API Token哈希失败", e);
        }
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request 请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        String remoteIp = getRemoteIp(request);
        return getClientIp(request, remoteIp);
    }

    private String getClientIp(ServerHttpRequest request, String remoteIp) {
        if (!isTrustedProxy(remoteIp)) {
            return remoteIp;
        }
        String forwardedClientIp = resolveForwardedClientIp(request);
        return StringUtils.defaultIfBlank(forwardedClientIp, remoteIp);
    }

    private String getRemoteIp(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null) {
            return UNKNOWN_IP;
        }
        if (remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return StringUtils.defaultIfBlank(remoteAddress.getHostString(), UNKNOWN_IP);
    }

    private String resolveForwardedClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst(X_FORWARDED_FOR);
        String clientIp = resolveFromXForwardedFor(xForwardedFor);
        if (StringUtils.isNotBlank(clientIp)) {
            return clientIp;
        }
        return normalizeHeaderIp(request.getHeaders().getFirst(X_REAL_IP));
    }

    private String resolveFromXForwardedFor(String xForwardedFor) {
        if (StringUtils.isBlank(xForwardedFor)) {
            return null;
        }
        String[] ipChain = xForwardedFor.split(",");
        for (String ip : ipChain) {
            String currentIp = normalizeHeaderIp(ip);
            if (isIpLiteral(currentIp)) {
                return currentIp;
            }
        }
        return null;
    }

    private String normalizeHeaderIp(String rawIp) {
        if (StringUtils.isBlank(rawIp)) {
            return null;
        }
        String ip = rawIp.trim();
        if (UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return null;
        }
        if (ip.startsWith("[") && ip.contains("]")) {
            return ip.substring(1, ip.indexOf(']'));
        }
        int firstColonIndex = ip.indexOf(':');
        if (firstColonIndex > 0 && ip.indexOf(':', firstColonIndex + 1) < 0) {
            String hostPart = ip.substring(0, firstColonIndex);
            if (ipv4ToLong(hostPart) >= 0) {
                return hostPart;
            }
        }
        return ip;
    }

    private boolean isTrustedProxy(String ip) {
        if (!isIpLiteral(ip) || StringUtils.isBlank(trustedProxyCidrs)) {
            return false;
        }
        String[] cidrs = trustedProxyCidrs.split(",");
        for (String cidr : cidrs) {
            if (matchesTrustedProxy(ip, cidr)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesTrustedProxy(String ip, String cidr) {
        if (StringUtils.isBlank(cidr)) {
            return false;
        }
        String trustedRange = cidr.trim();
        if (!trustedRange.contains("/")) {
            return ip.equals(trustedRange);
        }
        String[] rangeParts = trustedRange.split("/");
        if (rangeParts.length != 2) {
            return false;
        }
        long ipValue = ipv4ToLong(ip);
        long rangeValue = ipv4ToLong(rangeParts[0]);
        if (ipValue < 0 || rangeValue < 0) {
            // 可信代理 CIDR 当前只支持 IPv4；IPv6 代理按非可信处理，避免误信任代理头。
            return false;
        }
        try {
            int prefixLength = Integer.parseInt(rangeParts[1]);
            if (prefixLength < 0 || prefixLength > IPV4_BIT_LENGTH) {
                return false;
            }
            long mask = prefixLength == 0 ? 0 : 0xFFFFFFFFL << (IPV4_BIT_LENGTH - prefixLength) & 0xFFFFFFFFL;
            return (ipValue & mask) == (rangeValue & mask);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isIpLiteral(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        return ipv4ToLong(ip) >= 0 || ip.contains(":") && ip.matches("^[0-9a-fA-F:.%]+$");
    }

    private long ipv4ToLong(String ip) {
        if (StringUtils.isBlank(ip)) {
            return -1L;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return -1L;
        }
        long value = 0L;
        for (String part : parts) {
            if (!part.matches("\\d{1,3}")) {
                return -1L;
            }
            int number = Integer.parseInt(part);
            if (number < 0 || number > 255) {
                return -1L;
            }
            value = (value << 8) + number;
        }
        return value;
    }

    /**
     * 检查路径权限
     *
     * @param uri 请求URI
     * @param pathList 权限路径列表
     * @return 是否有权限
     */
    private boolean checkPathPermission(String uri, String[] pathList) {
        if (pathList == null || pathList.length == 0) {
            return true; // 没有权限限制，默认通过
        }

        for (String path : pathList) {
            if (StringUtils.isNotBlank(path)) {
                // 支持通配符匹配
                if (path.endsWith("/**")) {
                    String prefix = path.substring(0, path.length() - 3);
                    if (uri.startsWith(prefix)) {
                        return true;
                    }
                } else if (path.endsWith("/*")) {
                    String prefix = path.substring(0, path.length() - 2);
                    if (uri.startsWith(prefix) && uri.substring(prefix.length()).indexOf('/') == -1) {
                        return true;
                    }
                } else if (uri.equals(path)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void parseFormDataToken(ServerWebExchange exchange, ServerHttpRequest request) {
        // 缓存获取
        GatewayContext<?> gatewayContext = exchange.getAttribute(GatewayContext.CACHE_GATEWAY_CONTEXT);
        if (null != gatewayContext) {
            String data = gatewayContext.getRequestBody();
//            log.info("埋点接口待解析数据:{}", data);
            if (StringUtils.isNotBlank(data)) {
                // 解析 JSON 获取 token 字段
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(data));
                    String token = jsonObject.getOrDefault("token", "").toString();
                    if (StringUtils.isNotBlank(token)) {
                        //解析token
                        LoginUser loginUser = tokenService.getLoginUser(token);
                        if (Objects.isNull(loginUser)) {
//                            ServiceException.runError(ApiError.ERROR_FORBIDDEN.getMsg());
                            log.info("埋点接口token失效:{}", data);
                            return;
                        }
                        loginUser.setAccessToken(token);
                        // 只传后端需要的基础字段，不传 permissionList/menuList，避免 header 过大导致 HTTP 解析异常
                        String tokenUserInfo = LoginUser.simpleLoginUser(loginUser);
                        request.mutate().header("tokenUserInfo", tokenUserInfo).build();
//                        log.info("埋点接口token解析成功:{}", data);
                    }
//                    log.warn("埋点接口未找到前端提交的token:{}", data);
                } catch (Exception e) {
                    log.error("埋点接口解析token 失败:{}", ExceptionUtil.stacktraceToString(e));
                    e.printStackTrace();
                }
            }
        }
    }
}
