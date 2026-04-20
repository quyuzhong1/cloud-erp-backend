package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.sys.dto.RedisDTO;
import com.erp.server.sys.service.ErpRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zdy
 * @ClassName ErpRedisServiceImpl
 * @description: TODO
 * @date 2026年04月02日
 * @version: 1.0
 */
@Slf4j
@Service
public class ErpRedisServiceImpl implements ErpRedisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean copyKey(String sourcePrefix, String targetPrefix) {
        log.info("copyKey sourcePrefix: {}, targetPrefix: {}", sourcePrefix, targetPrefix);
        // 1. 模糊查询所有匹配的KEY
        Set<String> sourceKeys = redisTemplate.keys(sourcePrefix + "*");
        if (CollUtil.isEmpty(sourceKeys)) {
            return Boolean.TRUE;
        }

        // 2. 循环批量复制
        for (String sourceKey : sourceKeys) {
            try {
                // 生成新KEY：替换前缀
                String targetKey = sourceKey.replace(sourcePrefix, targetPrefix);

                // 3. 复制数据 + 过期时间（核心方法）
                copyRedisKey(sourceKey, targetKey);
            } catch (Exception e) {
                // 单个KEY失败不影响整体
                e.printStackTrace();
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 单个KEY复制（兼容Spring Data Redis 2.3.4）
     * 原KEY不动，生成新KEY，同步数据+TTL
     */
    private void copyRedisKey(String sourceKey, String targetKey) {
        // 1. 序列化导出源KEY数据
        byte[] dataBytes = redisTemplate.dump(sourceKey);
        if (dataBytes == null) {
            return;
        }
        // 存在就删除，强制覆盖
        if (Boolean.TRUE.equals(redisTemplate.hasKey(targetKey))) {
            redisTemplate.delete(targetKey);
        }
        // 2. 获取剩余过期时间（秒）
        Long expireSeconds = redisTemplate.getExpire(sourceKey);
        if (expireSeconds == null || expireSeconds < 0) {
            expireSeconds = 0L; // 0 = 永久有效
        }

        // 3. 反序列化导入到目标KEY（原KEY不动）
        redisTemplate.restore(targetKey, dataBytes, expireSeconds.intValue(), TimeUnit.SECONDS);
    }
}
