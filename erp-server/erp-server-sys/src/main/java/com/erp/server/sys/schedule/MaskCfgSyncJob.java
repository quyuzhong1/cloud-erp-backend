package com.erp.server.sys.schedule;

import com.erp.server.sys.service.MaskCfgSyncService;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏规则配置定时对账任务
 *
 * <p>定位：{@code CfgMaskFieldService} / {@code CfgMaskWordService} "service 层写完即延迟双删"机制
 * 的<b>兜底层</b>，覆盖"运维直接 SQL 改库 / 迁移脚本 / 跨服务直接写表"等绕过 service 的写入路径。</p>
 *
 * <h3>XXL-JOB 推荐配置</h3>
 * <table>
 *   <tr><th>项</th><th>建议值</th><th>说明</th></tr>
 *   <tr><td>JobHandler</td><td>{@code maskCfgSyncJob}</td><td>对应 {@link XxlJob#value()}</td></tr>
 *   <tr><td>Cron</td><td>{@code 0/30 * * * * ?}</td>
 *       <td>每 30 秒一次。脱敏规则变更频率很低（运维手工配），30s 延迟够用</td></tr>
 *   <tr><td>路由策略</td><td><b>FIRST</b></td>
 *       <td>避免多 Pod 重复扫表浪费 DB；单节点回填 Redis 足够</td></tr>
 *   <tr><td>阻塞处理策略</td><td>DISCARD_LATER</td>
 *       <td>上一次没跑完就跳过本次，避免快照状态错乱</td></tr>
 *   <tr><td>失败重试</td><td>0</td>
 *       <td>service 内部已容错，单次失败下次扫描自动恢复，不必触发重试</td></tr>
 * </table>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskCfgSyncJob {

    @Resource
    private MaskCfgSyncService maskCfgSyncService;

    @XxlJob("maskCfgSyncJob")
    public ReturnT<String> execute() {
        XxlJobHelper.log("====maskCfgSyncJob 开始任务=====");
        Map<String, Object> stats = maskCfgSyncService.runSync();
        XxlJobHelper.log("success={}, firstRun={}, elapsedMs={}, "
                + "cfgMaskFieldChanged={}, cfgMaskWordChanged={}",
                stats.get("success"), stats.get("firstRun"), stats.get("elapsedMs"),
                stats.get("cfgMaskFieldChanged"), stats.get("cfgMaskWordChanged"));
        if (Boolean.FALSE.equals(stats.get("success"))) {
            XxlJobHelper.log("error={}", stats.get("errorMsg"));
        }
        XxlJobHelper.log("====maskCfgSyncJob 结束任务=====");
        // service 内部已容错；总是返回 SUCCESS 避免 XXL-JOB 告警噪音
        // 真正的失败要看 stats.success 字段（已写入 XXL-JOB 日志）
        return ReturnT.SUCCESS;
    }
}
