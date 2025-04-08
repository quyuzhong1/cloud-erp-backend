package com.common.core.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ip 工具类
 *
 * @Classname IpUtils

 * @Date 2022-07-08 15:54
 * @Created by yl
 */
@Slf4j
public class IpUtils {

    /**
     * 常量 定义 unknown
     */
    public static final String UNKNOWN = "unknown";

    /**
     * 常量 定义本地IP ipv4 形式
     */
    public static final String LOCALHOST_IPV4 = "127.0.0.1";

    /**
     * 常量 获取IP地址方法中的IP长度
     */
    public static final int IP_LENGTH = 15;

    /**
     * 常量 逗号
     */
    public static final String COMMA = ",";


    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";

    private static final String LOCALHOST_IP1 = "127.0.0.1";

    /**
     * 获取IP地址
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个
     * 而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串
     * 则为真实IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (ip.equals(LOCALHOST_IPV4)) {
                    //根据网卡取本机配置的IP
                    InetAddress inetAddress = null;
                    try {
                        inetAddress = InetAddress.getLocalHost();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ip = inetAddress.getHostAddress();
                }
            }
            // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ip != null && ip.length() > IP_LENGTH) {
                if (ip.indexOf(COMMA) > 0) {
                    ip = ip.substring(0, ip.indexOf(COMMA));
                }
            }
            return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
        } catch (Exception e) {
            log.error("获取IP地址时出现异常");
            log.error("" + e);
            return "-1";
        }
    }


    /**
     * 获取IP地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        try {
            ip = request.getHeader("true-client-ip");
            // 以下两个获取在k8s中，将真实的客户端IP，放到了x-Original-Forwarded-For。而将WAF的回源地址放到了 x-Forwarded-For了。
            if (checkLocalIp(ip)) {
                ip = request.getHeader("X-Original-Forwarded-For");
            }
            if (checkLocalIp(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }
            // 获取nginx等代理的ip
            if (checkLocalIp(ip)) {
                ip = request.getHeader("x-forwarded-for");
            }
            if (checkLocalIp(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (checkLocalIp(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (checkLocalIp(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (checkLocalIp(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (checkLocalIp(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (checkLocalIp(ip)) {
                // 兼容k8s集群获取ip
                ip = request.getRemoteAddr();
                if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
                    // 根据网卡取本机配置的IP
                    InetAddress iNet = null;
                    try {
                        iNet = InetAddress.getLocalHost();
                        ip = iNet.getHostAddress();
                    } catch (UnknownHostException e) {
                        log.error("getClientIp error!", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("IpUtil ERROR", e);
        }
        // 使用代理，则获取第一个IP地址
        if (org.springframework.util.StringUtils.hasText(ip) && ip.indexOf(COMMA) > 0) {
            ip = ip.substring(0, ip.indexOf(COMMA));
        }

        return ip;
    }

    private static boolean checkLocalIp(String ip) {
        return !org.springframework.util.StringUtils.hasText(ip)
                || UNKNOWN.equalsIgnoreCase(ip)
                || LOCALHOST_IP1.equalsIgnoreCase(ip)
                || LOCALHOST_IP.equalsIgnoreCase(ip);
    }
}
