package com.erp.server.sys.service.impl;

import com.erp.server.sys.mapper.MaskCfgSyncMapper;
import com.erp.server.sys.service.CfgMaskFieldService;
import com.erp.server.sys.service.CfgMaskWordService;
import com.erp.server.sys.service.MaskCfgSyncService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link MaskCfgSyncService} 实现：两张 cfg 表整表 hash 比对
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li><b>首次扫描不刷新 Redis</b>：服务启动后第一次跑只填充快照，避免冷启动时"误认为全表都变了"
 *       触发一次无意义的 Redis 回填。{@link #firstRun} 处理。</li>
 *   <li><b>每张表独立刷新 + 独立容错</b>：一张表刷新失败不影响另一张表；
 *       下一次扫描如果还是不一致，会继续重试。</li>
 *   <li><b>整体替换快照在刷新 Redis 后做</b>：避免刷新失败但快照已经更新，导致下次扫描
 *       误以为已经同步过而不再补发。</li>
 *   <li><b>task 内部全部捕获异常</b>：单次扫描失败下次自动恢复，不抛给 XXL-JOB 避免告警噪音。</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Slf4j
@Service
public class MaskCfgSyncServiceImpl implements MaskCfgSyncService {

    @Resource
    private MaskCfgSyncMapper maskCfgSyncMapper;

    @Autowired(required = false)
    private CfgMaskFieldService cfgMaskFieldService;

    @Autowired(required = false)
    private CfgMaskWordService cfgMaskWordService;

    /** cfg_mask_field 上次扫描后的整表 hash */
    private volatile String cfgMaskFieldHashSnapshot = "";

    /** cfg_mask_word 上次扫描后的整表 hash */
    private volatile String cfgMaskWordHashSnapshot = "";

    /** 首次运行标志。冷启动只填充快照不刷新 Redis。 */
    private volatile boolean firstRun = true;

    /** 最近一次同步的统计（诊断用） */
    private volatile Map<String, Object> lastStats = Collections.emptyMap();

    @Override
    public Map<String, Object> runSync() {
        long start = System.currentTimeMillis();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("startMs", start);
        stats.put("firstRun", firstRun);
        try {
            String currentFieldHash = nullToEmpty(maskCfgSyncMapper.scanCfgMaskFieldHash());
            String currentWordHash = nullToEmpty(maskCfgSyncMapper.scanCfgMaskWordHash());

            if (firstRun) {
                cfgMaskFieldHashSnapshot = currentFieldHash;
                cfgMaskWordHashSnapshot = currentWordHash;
                firstRun = false;
                stats.put("cfgMaskFieldChanged", false);
                stats.put("cfgMaskWordChanged", false);
                stats.put("success", true);
                log.info("MaskCfgSyncJob firstRun fill snapshot: fieldHash={}, wordHash={}",
                        shortHash(currentFieldHash), shortHash(currentWordHash));
                return stats;
            }

            boolean fieldChanged = !Objects.equals(cfgMaskFieldHashSnapshot, currentFieldHash);
            stats.put("cfgMaskFieldChanged", fieldChanged);
            if (fieldChanged) {
                log.info("MaskCfgSyncJob cfg_mask_field hash changed: {} -> {}",
                        shortHash(cfgMaskFieldHashSnapshot), shortHash(currentFieldHash));
                safelyPublishCfgMaskField();
            }

            boolean wordChanged = !Objects.equals(cfgMaskWordHashSnapshot, currentWordHash);
            stats.put("cfgMaskWordChanged", wordChanged);
            if (wordChanged) {
                log.info("MaskCfgSyncJob cfg_mask_word hash changed: {} -> {}",
                        shortHash(cfgMaskWordHashSnapshot), shortHash(currentWordHash));
                safelyPublishCfgMaskWord();
            }

            cfgMaskFieldHashSnapshot = currentFieldHash;
            cfgMaskWordHashSnapshot = currentWordHash;
            stats.put("success", true);
        } catch (Throwable e) {
            log.warn("MaskCfgSyncJob failed, snapshot keeps unchanged, next run retry", e);
            stats.put("success", false);
            stats.put("errorMsg", e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            stats.put("elapsedMs", System.currentTimeMillis() - start);
            lastStats = stats;
        }
        return stats;
    }

    @Override
    public Map<String, Object> snapshotView() {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("firstRun", firstRun);
        view.put("cfgMaskFieldHash", shortHash(cfgMaskFieldHashSnapshot));
        view.put("cfgMaskWordHash", shortHash(cfgMaskWordHashSnapshot));
        view.put("cfgMaskFieldServiceAvailable", cfgMaskFieldService != null);
        view.put("cfgMaskWordServiceAvailable", cfgMaskWordService != null);
        view.put("lastStats", lastStats);
        return view;
    }

    @Override
    public void resetSnapshot() {
        cfgMaskFieldHashSnapshot = "";
        cfgMaskWordHashSnapshot = "";
        firstRun = true;
        log.info("MaskCfgSyncService snapshot reset, next run will treat as firstRun");
    }

    /**
     * 调 cfg_mask_field 全量 Redis 回填，失败只 warn，不抛
     */
    private void safelyPublishCfgMaskField() {
        if (cfgMaskFieldService == null) {
            log.warn("CfgMaskFieldService not available, skip redis refresh");
            return;
        }
        try {
            cfgMaskFieldService.publishFullCache();
        } catch (Throwable e) {
            log.warn("refresh cfg_mask_field redis cache failed, next run will retry on diff", e);
        }
    }

    /**
     * 调 cfg_mask_word 全量 Redis 回填，失败只 warn，不抛
     */
    private void safelyPublishCfgMaskWord() {
        if (cfgMaskWordService == null) {
            log.warn("CfgMaskWordService not available, skip redis refresh");
            return;
        }
        try {
            cfgMaskWordService.publishFullCache();
        } catch (Throwable e) {
            log.warn("refresh cfg_mask_word redis cache failed, next run will retry on diff", e);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * 截断 hash 仅展示前 8 字符，避免日志 / 接口输出 32 字符全 hash
     */
    private static String shortHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return "";
        }
        return hash.length() <= 8 ? hash : hash.substring(0, 8);
    }
}
