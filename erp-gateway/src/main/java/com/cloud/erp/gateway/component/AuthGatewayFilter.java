package com.cloud.erp.gateway.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.erp.gateway.utils.ServletUtils;
import com.cloud.erp.gateway.web.server.TokenService;
import com.common.business.constant.AuthPassPath;
import com.common.business.constant.TokenConstants;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname AuthGatewayFilter

 * @Date 2022-07-11 11:38
 * @Created by yl
 */
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

    @Resource
    private TokenService tokenService;


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
            //判断是否是app 如果是 直接放行
            if(uri.contains(OPEN_API_URL)){
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
                return parseRequestBodyToken(exchange, chain, request);
            }

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
            loginUser.setAccessToken(token);
            request.mutate().header("tokenUserInfo", URLEncoder.encode(JSON.toJSONString(loginUser), "UTF-8")).build();
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

    private Mono<Void> parseRequestBodyToken(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest request) {
        // 获取 POST 请求体的Token内容
        return exchange.getRequest().getBody()
                .collectList()  // 收集所有的数据块
                .publishOn(Schedulers.boundedElastic())  // 收集所有的数据块
                .flatMap(dataBuffers -> {
                    // 将请求体内容合并为一个字符串
                    String body = dataBuffers.stream()
                            .map(dataBuffer -> dataBuffer.toString(StandardCharsets.UTF_8))
                            .reduce((s1, s2) -> s1 + s2)
                            .orElse("");
                    try {
                        // 解析 JSON 获取 token 字段
                        JSONObject jsonObject = JSON.parseObject(body);
                        String token = jsonObject.getOrDefault("token", "").toString();
                        if (StringUtils.isNotBlank(token)) {
                            //解析token
                            LoginUser loginUser = tokenService.getLoginUser(token);
                            if (Objects.isNull(loginUser)) {
                                return unauthorizedResponse(exchange, ApiError.ERROR_403.msg, ApiError.ERROR_403.code);
                            }
                            loginUser.setAccessToken(token);
                            request.mutate().header("tokenUserInfo", URLEncoder.encode(JSON.toJSONString(loginUser), "UTF-8")).build();
                        }
                    } catch (Exception e) {
                        return Mono.error(e); // 处理解析异常
                    }
                    // 继续处理过滤链
                    return chain.filter(exchange);
                });
    }
}
