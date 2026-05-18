package com.erp.server.sys.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.sys.service.MaskCfgSyncService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 脱敏规则对账诊断接口
 *
 * <p>暴露 {@link MaskCfgSyncService} 的运维操作：</p>
 * <ul>
 *   <li>{@code GET /maskCfgSync/snapshot}：看当前内存快照状态 + 上次任务统计</li>
 *   <li>{@code POST /maskCfgSync/run}：手动触发一次同步（XXL-JOB 控制台之外的备用入口）</li>
 *   <li>{@code POST /maskCfgSync/reset}：重置快照，下次扫描重新填充（用于"怀疑快照脏了"场景）</li>
 * </ul>
 *
 * @author cloud-erp
 */
@RestController
@RequestMapping("maskCfgSync")
public class MaskCfgSyncController extends BaseController {

    @Resource
    private MaskCfgSyncService maskCfgSyncService;

    @GetMapping("/snapshot")
    public ApiResult<Map<String, Object>> snapshot() {
        return success(maskCfgSyncService.snapshotView());
    }

    /**
     * 手动触发一次同步。生产慎用——会瞬时扫表 + 必要时刷新 Redis 配置缓存。
     *
     * <p>典型场景：刚改完脱敏规则想立刻验证；运维改完库想强制对账。</p>
     */
    @PostMapping("/run")
    public ApiResult<Map<String, Object>> run() {
        return success(maskCfgSyncService.runSync());
    }

    /**
     * 重置内存快照。下一次扫描会触发 firstRun 分支，只填充快照不刷新 Redis。
     *
     * <p>典型场景：怀疑快照状态错乱（如 publish 失败后又恢复），重置后做"全量对账"。</p>
     */
    @PostMapping("/reset")
    public ApiResult<String> reset() {
        maskCfgSyncService.resetSnapshot();
        return success("snapshot reset; next run will treat as firstRun");
    }
}
