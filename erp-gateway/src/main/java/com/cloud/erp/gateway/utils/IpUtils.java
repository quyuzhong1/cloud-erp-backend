package com.cloud.erp.gateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author GitEgg
 */
@Slf4j
public class IpUtils {

    private IpUtils() {
    }

    /**
     * 反向代理分割符
     */
    private static final String IP_UTILS_FLAG = ",";

    private static final String UNKNOWN = "unknown";

    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";

    private static final String LOCALHOST_IP1 = "127.0.0.1";

    public static String getIP(ServerHttpRequest request) {
        // 根据 HttpHeaders 获取 请求 IP地址
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("x-forwarded-for");
            if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip) && ip.contains(IP_UTILS_FLAG)) {
                    ip = ip.split(IP_UTILS_FLAG)[0];
                }

        }

        ip = getIpByHeaders(request, ip);

        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            if (null == request.getRemoteAddress()) {
                ip = request.getRemoteAddress().getAddress().getHostAddress();
            }
            if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
                InetAddress iNet;
                try {
                    iNet = InetAddress.getLocalHost();
                    ip = iNet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("getClientIp error: ", e);
                }
            }
        }
        return ip;
    }


    private static String getIpByHeaders(ServerHttpRequest request, String ip) {
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        return ip;
    }
}
