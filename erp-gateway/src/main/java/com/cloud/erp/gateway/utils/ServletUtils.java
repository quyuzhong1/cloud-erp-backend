package com.cloud.erp.gateway.utils;

import com.alibaba.fastjson2.JSON;
import com.common.core.constant.CommonConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.ConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

/**
 * @Classname ServletUtils
 * @Date 2022-07-15 10:29
 * @Created by yl
 */
public class ServletUtils {

    private ServletUtils() {
    }


    /**
     * 获取String参数
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return null == request ? null : request.getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue) {
        return ConvertUtil.toStr(getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name) {
        return ConvertUtil.toInt(getParameter(name));
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return ConvertUtil.toInt(getParameter(name), defaultValue);
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name) {
        return ConvertUtil.toBool(getParameter(name));
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        return ConvertUtil.toBool(getParameter(name), defaultValue);
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes requestAttributes = getRequestAttributes();
            if (null == requestAttributes) {
                return null;
            }
            return requestAttributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse() {
        try {
            ServletRequestAttributes requestAttributes = getRequestAttributes();
            if (null == requestAttributes) {
                return null;
            }
            return requestAttributes.getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取session
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        if (null == request) {
            return null;
        }
        return request.getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return urlDecode(value);
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedCaseInsensitiveMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, CommonConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, CommonConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value) {
        return webFluxResponseWriter(response, HttpStatus.OK, value, ApiError.Default.code);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param code     响应状态码
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value, int code) {
        return webFluxResponseWriter(response, HttpStatus.OK, value, code);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param status   http状态码
     * @param code     响应状态码
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, HttpStatus status, Object value, int code) {
        return webFluxResponseWriter(response, MediaType.APPLICATION_JSON_VALUE, status, value, code);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response    ServerHttpResponse
     * @param contentType content-type
     * @param status      http状态码
     * @param code        响应状态码
     * @param value       响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String contentType, HttpStatus status, Object value, int code) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);
        ApiResult<?> result = ApiResult.error(code, value.toString());
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        Mono<DataBuffer> just = Mono.just(dataBuffer);
        Mono<Void> voidMono = response.writeWith(just);
        DataBufferUtils.release(dataBuffer);
        return voidMono;
    }
}
