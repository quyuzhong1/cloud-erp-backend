package com.cloud.erp.gateway.controller;

import com.cloud.erp.gateway.utils.IpRateLimitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * IP访问频率限制管理接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
@RestController
@RequestMapping("/gateway/ip-limit")
public class IpRateLimitController {

    @Resource
    private IpRateLimitUtil ipRateLimitUtil;

    /**
     * 获取IP访问统计信息
     *
     * @param ipAddress IP地址
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getIpStats(@RequestParam String ipAddress) {
        Map<String, Object> result = new HashMap<>();
        try {
            String stats = ipRateLimitUtil.getIpStats(ipAddress);
            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            log.error("获取IP统计信息失败", e);
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 手动封禁IP
     *
     * @param ipAddress IP地址
     * @param blockTime 封禁时间（秒）
     * @return 操作结果
     */
    @PostMapping("/block")
    public Map<String, Object> blockIp(@RequestParam String ipAddress, 
                                      @RequestParam(defaultValue = "300") int blockTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            ipRateLimitUtil.blockIp(ipAddress, blockTime);
            result.put("success", true);
            result.put("message", "IP " + ipAddress + " 已封禁 " + blockTime + " 秒");
        } catch (Exception e) {
            log.error("封禁IP失败", e);
            result.put("success", false);
            result.put("message", "封禁IP失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 解除IP封禁
     *
     * @param ipAddress IP地址
     * @return 操作结果
     */
    @PostMapping("/unblock")
    public Map<String, Object> unblockIp(@RequestParam String ipAddress) {
        Map<String, Object> result = new HashMap<>();
        try {
            ipRateLimitUtil.unblockIp(ipAddress);
            result.put("success", true);
            result.put("message", "IP " + ipAddress + " 已解除封禁");
        } catch (Exception e) {
            log.error("解除IP封禁失败", e);
            result.put("success", false);
            result.put("message", "解除IP封禁失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 清理过期记录
     *
     * @return 操作结果
     */
    @PostMapping("/clean")
    public Map<String, Object> cleanExpiredRecords() {
        Map<String, Object> result = new HashMap<>();
        try {
            ipRateLimitUtil.cleanExpiredRecords();
            result.put("success", true);
            result.put("message", "过期记录清理完成");
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
            result.put("success", false);
            result.put("message", "清理过期记录失败: " + e.getMessage());
        }
        return result;
    }
}
