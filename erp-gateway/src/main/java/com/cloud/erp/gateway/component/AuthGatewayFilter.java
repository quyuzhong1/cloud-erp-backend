package com.cloud.erp.gateway.component;

import cn.hutool.core.exceptions.ExceptionUtil;
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
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
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
import java.util.List;
import java.util.Objects;
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

    private static final String PERSONAL_CENTER_API_TOKEN_PATH = "/personalCenter/apiToken";

    private static final String API_TOKEN_WHITELIST_PATH = "/apiTokenWhitelist";

    private static final String[] API_TOKEN_DENY_PATHS = {
            PERSONAL_CENTER_API_TOKEN_PATH,
            API_TOKEN_WHITELIST_PATH,
            FEIGN_URL
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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {  //获取请求
            ServerHttpRequest request = stripApiTokenInternalHeaders(exchange.getRequest());
            exchange = exchange.mutate().request(request).build();
            // 获取请求URL
            String uri = request.getPath().value();
            //判断是否有feign
            if (uri.contains(FEIGN_URL)) {
                //文件头使用JSON格式
                return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_FORBIDDEN, exchange.getRequest()), ApiError.HTTP_FORBIDDEN.getCode());
            }
            Mono<Void> apiTokenAuthResult = tryApiTokenAuth(exchange, chain, request, uri);
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
            if (AuthPassPath.EVENT_TRACKING_PATH.contains(uri)) {
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

    private Mono<Void> tryApiTokenAuth(ServerWebExchange exchange, GatewayFilterChain chain,
                                       ServerHttpRequest request, String uri) {
        String apiToken = resolveApiTokenFromAuthorization(request.getHeaders().getFirst(TokenConstants.AUTHENTICATION));
        if (StringUtils.isBlank(apiToken)) {
            return null;
        }
        if (isApiTokenManagementPath(uri)) {
            return unauthorizedResponse(exchange, "API Token不允许访问管理接口", ApiError.HTTP_FORBIDDEN.getCode());
        }
        if (!isApiTokenFormatValid(apiToken)) {
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
        }

        String tokenHash = sha256Hex(apiToken);
        // API Token 是入口级能力，先做 IP/全局/路径限流，再调用 sys 服务校验，避免异常流量直接压到 Feign。
        Mono<Void> rateLimitResult = checkApiTokenRateLimit(exchange, request, uri);
        if (rateLimitResult != null) {
            return rateLimitResult;
        }
        if (ipRateLimitUtil.isApiTokenFailureBlocked(tokenHash)) {
            log.warn("API Token失败次数过多，已临时封禁，URI: {}", uri);
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
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
                    return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
                }
                cacheApiTokenValidate(tokenHash, uri, validateResp);
            }
            if (!Boolean.TRUE.equals(validateResp.getTokenValid())) {
                recordApiTokenFailure(tokenHash, uri, "invalid");
                return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
            }
            if (!Boolean.TRUE.equals(validateResp.getPathAllowed())) {
                log.warn("API Token接口未配置白名单，URI: {}, tokenId: {}", uri, validateResp.getTokenId());
                recordApiTokenFailure(tokenHash, uri, "forbidden");
                return unauthorizedResponse(exchange, "接口未配置API Token白名单", ApiError.HTTP_FORBIDDEN.getCode());
            }

            LoginUser loginUser = buildLoginUser(validateResp);
            String tokenUserInfo = LoginUser.simpleLoginUser(loginUser);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove(TokenConstants.AUTHENTICATION);
                        headers.remove(SysApiTokenConstants.INTERNAL_AUTH_HEADER);
                        headers.remove(SysApiTokenConstants.INTERNAL_TOKEN_ID_HEADER);
                    })
                    .header("tokenUserInfo", tokenUserInfo)
                    .header(SysApiTokenConstants.INTERNAL_AUTH_HEADER, SysApiTokenConstants.INTERNAL_AUTH_VALUE)
                    .header(SysApiTokenConstants.INTERNAL_TOKEN_ID_HEADER, validateResp.getTokenId())
                    .build();
            ServerWebExchange authenticatedExchange = exchange.mutate().request(mutatedRequest).build();
            // 签名过滤器只信任 exchange 内部属性，不信任客户端可伪造的同名请求头。
            authenticatedExchange.getAttributes().put(SysApiTokenConstants.INTERNAL_AUTH_ATTRIBUTE, Boolean.TRUE);
            return chain.filter(authenticatedExchange);
        } catch (UnsupportedEncodingException e) {
            log.error("API Token用户信息编码失败，URI: {}", uri, e);
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNKNOWN, exchange.getRequest()), ApiError.HTTP_UNKNOWN.getCode());
        } catch (Exception e) {
            log.error("API Token校验失败，URI: {}", uri, e);
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_UNAUTHORIZED, exchange.getRequest()), ApiError.HTTP_UNAUTHORIZED.getCode());
        }
    }

    private Mono<Void> checkApiTokenRateLimit(ServerWebExchange exchange, ServerHttpRequest request, String uri) {
        Mono<Void> ipRateLimitResult = checkOpenApiRateLimit(exchange, request, uri);
        if (ipRateLimitResult != null) {
            return ipRateLimitResult;
        }
        if (!ipRateLimitUtil.isApiTokenGlobalAllowed()) {
            log.warn("API Token全局访问频率过高，URI: {}", uri);
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_TOO_MANY_REQUESTS, exchange.getRequest()), ApiError.HTTP_TOO_MANY_REQUESTS.getCode());
        }
        if (!ipRateLimitUtil.isApiTokenPathAllowed(sha256Hex(uri))) {
            log.warn("API Token路径访问频率过高，URI: {}", uri);
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_TOO_MANY_REQUESTS, exchange.getRequest()), ApiError.HTTP_TOO_MANY_REQUESTS.getCode());
        }
        return null;
    }

    private Mono<Void> checkOpenApiRateLimit(ServerWebExchange exchange, ServerHttpRequest request, String uri) {
        String clientIp = getClientIp(request);
        if (!ipRateLimitUtil.isOpenApiAllowed(clientIp)) {
            log.warn("开放接口访问频率过高或限流组件不可用，IP: {}, URI: {}", clientIp, uri);
            return unauthorizedResponse(exchange, localeUtils.getMessage(ApiError.HTTP_TOO_MANY_REQUESTS, exchange.getRequest()), ApiError.HTTP_TOO_MANY_REQUESTS.getCode());
        }
        return null;
    }

    private ServerHttpRequest stripApiTokenInternalHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(SysApiTokenConstants.INTERNAL_AUTH_HEADER);
                    headers.remove(SysApiTokenConstants.INTERNAL_TOKEN_ID_HEADER);
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
        for (String denyPath : API_TOKEN_DENY_PATHS) {
            if (uri.contains(denyPath)) {
                return true;
            }
        }
        return false;
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
        // 限流和封禁只使用直接连接地址，避免客户端伪造 X-Forwarded-For 等头影响封禁目标。
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null) {
            return "unknown";
        }
        if (remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return StringUtils.defaultIfBlank(remoteAddress.getHostString(), "unknown");
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
