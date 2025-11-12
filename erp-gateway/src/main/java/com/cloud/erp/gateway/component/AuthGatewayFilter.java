package com.cloud.erp.gateway.component;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.cloud.erp.gateway.config.JwtProperties;
import com.cloud.erp.gateway.context.GatewayContext;
import com.cloud.erp.gateway.utils.IpRateLimitUtil;
import com.cloud.erp.gateway.utils.ServletUtils;
import com.cloud.erp.gateway.web.server.TokenService;
import com.common.business.constant.AuthPassPath;
import com.common.business.constant.TokenConstants;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname AuthGatewayFilter
 * @Date 2022-07-11 11:38
 * @Created by yl
 */
@Slf4j
@Component
public class AuthGatewayFilter implements GlobalFilter, Order {

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

    @Resource
    private TokenService tokenService;
    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private IpRateLimitUtil ipRateLimitUtil;


    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {  //获取请求
            ServerHttpRequest request = exchange.getRequest();
            // 获取请求URL
            String uri = request.getPath().value();
            //判断是否有feign
            if (uri.contains(FEIGN_URL)) {
                //文件头使用JSON格式
                return unauthorizedResponse(exchange, ApiError.ERROR_5001.msg, ApiError.ERROR_5001.code);
            }
            if (uri.contains(SSO_URL)||uri.contains(KEY_REGISTER_URL)){
                String clientIp = getClientIp(request);
                if (!ipRateLimitUtil.isOpenApiAllowed(clientIp)) {
                    log.warn("开放接口访问频率过高，IP: {}, URI: {}", clientIp, uri);
                    return unauthorizedResponse(exchange, ApiError.ERROR_429.msg, ApiError.ERROR_429.code);
                }
                return chain.filter(exchange);
            }
            //判断是否是开放API 如果是 进行IP防护后放行
            if (uri.contains(OPEN_API_URL)) {
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
                    Mono<Void> mono = unauthorizedResponse(exchange, ApiError.ERROR_403.msg, ApiError.ERROR_403.code);
                    return mono;
                }
                //解析token
                LoginUser loginUser = tokenService.getLoginUser(token);
                if (Objects.isNull(loginUser)) {
                    return unauthorizedResponse(exchange, ApiError.ERROR_403.msg, ApiError.ERROR_403.code);
                }
                
                // 检查JWT Token是否包含pathList权限
                String[] jwtPathList = com.erp.model.sys.utils.JwtUtils.getPathList(token, jwtProperties.getSecret());
                if (jwtPathList.length > 0) {
                    // JWT Token包含pathList权限，检查当前路径是否有权限
                    boolean hasPermission = checkPathPermission(uri, jwtPathList);
                    if (!hasPermission) {
                        log.warn("接口无权限，URI: {}, 用户权限: {}", uri, Arrays.toString(jwtPathList));
                        return unauthorizedResponse(exchange, "接口无权限", ApiError.ERROR_403.code);
                    }
                }
                
                loginUser.setAccessToken(token);
                request.mutate().header("tokenUserInfo", URLEncoder.encode(JSON.toJSONString(loginUser), "UTF-8")).build();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return chain.filter(exchange);
    }

    @Override
    public int value() {
        return 0;
    }


    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg, Integer code) {
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, code);
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request 请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        
        // 检查X-Forwarded-For头
        String xForwardedFor = headers.getFirst("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            String[] ips = xForwardedFor.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }
        
        // 检查X-Real-IP头
        String xRealIp = headers.getFirst("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        // 检查Proxy-Client-IP头
        String proxyClientIp = headers.getFirst("Proxy-Client-IP");
        if (StringUtils.isNotBlank(proxyClientIp) && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        // 检查WL-Proxy-Client-IP头
        String wlProxyClientIp = headers.getFirst("WL-Proxy-Client-IP");
        if (StringUtils.isNotBlank(wlProxyClientIp) && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        // 检查HTTP_CLIENT_IP头
        String httpClientIp = headers.getFirst("HTTP_CLIENT_IP");
        if (StringUtils.isNotBlank(httpClientIp) && !"unknown".equalsIgnoreCase(httpClientIp)) {
            return httpClientIp;
        }
        
        // 检查HTTP_X_FORWARDED_FOR头
        String httpXForwardedFor = headers.getFirst("HTTP_X_FORWARDED_FOR");
        if (StringUtils.isNotBlank(httpXForwardedFor) && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor;
        }
        
        // 最后使用远程地址
        String remoteAddress = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        
        return "unknown".equals(remoteAddress) ? "127.0.0.1" : remoteAddress;
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
//                            ServiceException.runError(ApiError.ERROR_403.msg);
                            log.info("埋点接口token失效:{}", data);
                            return;
                        }
                        loginUser.setAccessToken(token);
                        request.mutate().header("tokenUserInfo", URLEncoder.encode(JSON.toJSONString(loginUser), "UTF-8")).build();
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
