package com.sdk.third.tf.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.sdk.third.tf.util.JsonUtil;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * 税种工具类
 * 提供税种相关的工具方法
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class TaxCategoryUtil {

    /**
     * 计算税种详情的MD5值
     * 用于判断数据是否更新
     * 
     * @param detail 税种详情DTO
     * @return MD5值（32位小写十六进制字符串）
     */
    public static String calculateDataMd5(TaxCategoryDTO.CategoryDetailDTO detail) {
        if (detail == null) {
            return null;
        }
        
        try {
            // 将税种详情序列化为JSON字符串
            String jsonStr = JsonUtil.toJsonString(detail);
            
            // 计算MD5值
            String md5 = DigestUtil.md5Hex(jsonStr);
            
            log.debug("计算税种详情MD5, categoryId: {}, md5: {}", detail.getCategoryId(), md5);
            return md5;
        } catch (Exception e) {
            log.error("计算税种详情MD5失败, categoryId: {}", detail.getCategoryId(), e);
            throw new RuntimeException("计算税种详情MD5失败: " + e.getMessage());
        }
    }

    /**
     * 计算税种详情的MD5值（基于JSON字符串）
     * 
     * @param jsonStr 税种详情JSON字符串
     * @return MD5值（32位小写十六进制字符串）
     */
    public static String calculateDataMd5(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return null;
        }
        
        try {
            // 计算MD5值
            String md5 = DigestUtil.md5Hex(jsonStr);
            log.debug("计算JSON字符串MD5, md5: {}", md5);
            return md5;
        } catch (Exception e) {
            log.error("计算JSON字符串MD5失败", e);
            throw new RuntimeException("计算JSON字符串MD5失败: " + e.getMessage());
        }
    }

    /**
     * 比较两个MD5值是否一致
     * 
     * @param md51 MD5值1
     * @param md52 MD5值2
     * @return true表示一致，false表示不一致
     */
    public static boolean isMd5Equal(String md51, String md52) {
        if (md51 == null && md52 == null) {
            return true;
        }
        if (md51 == null || md52 == null) {
            return false;
        }
        return md51.equalsIgnoreCase(md52);
    }

    /**
     * 判断数据是否需要更新
     * 
     * @param localMd5 本地MD5值
     * @param remoteDetail 远程税种详情
     * @return true表示需要更新，false表示不需要更新
     */
    public static boolean needUpdate(String localMd5, TaxCategoryDTO.CategoryDetailDTO remoteDetail) {
        if (remoteDetail == null) {
            return false;
        }
        
        // 如果本地没有MD5，说明是新数据，需要更新
        if (StrUtil.isBlank(localMd5)) {
            return true;
        }
        
        // 计算远程数据的MD5
        String remoteMd5 = calculateDataMd5(remoteDetail);
        
        // 比较MD5，不一致则说明需要更新
        return !isMd5Equal(localMd5, remoteMd5);
    }
}
