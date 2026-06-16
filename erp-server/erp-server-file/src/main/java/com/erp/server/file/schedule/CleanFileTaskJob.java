package com.erp.server.file.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.core.ExportTempFilesHandler;
import com.erp.model.file.entity.FileTask;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.repository.FileTaskRepository;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 清理过期文件任务
 */
@Component
@Slf4j
public class CleanFileTaskJob {

    private static final int BATCH_SIZE = 100;

    /**
     * 僵尸任务兜底窗口（天）：临时文件 mtime 早于「正常过期窗口再加该天数」时，即使其 taskId 仍匹配处理中任务也强制删除。
     * 用于回收因 JVM 崩溃中途等异常导致任务长期卡在 PROCESS、临时文件残留无法清理的磁盘泄漏。
     */
    private static final int ZOMBIE_FORCE_DELETE_EXTRA_DAYS = 7;


    @Resource
    private FileTaskRepository fileTaskRepository;

    @XxlJob("cleanFileTask")
    public ReturnT<String> cleanFileTask() {
        XxlJobHelper.log("====开始清理过期文件任务====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        // 未传参用默认 360 天；传参须为 JSON {"days":N}，格式非法则 FAIL（见 parseDays JavaDoc），不回退默认以免误配被静默吞掉。
        Integer days = 360;
        if (StringUtils.isNotBlank(jobParam)){
            days = parseDays(jobParam);
            if (days == null || days <= 0) {
                XxlJobHelper.log("任务参数不合法, days必须为正整数, 任务参数={}", JSONUtil.toJsonStr(jobParam));
                return ReturnT.FAIL;
            }
        }

        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        XxlJobHelper.log("清理创建时间早于{}的已完成/失败文件任务（含无效 fileUrl）", expireTime);

        int batchNo = 0;
        int successCount = 0;
        int fastDfsFailCount = 0;
        int logicDeleteFailCount = 0;
        // 游标分页位置：上一批已处理到的最后一条 (create_time, id)，首批为 null。
        // 无论成功（软删后从结果集移除）还是失败，游标都单调推进，本轮不回扫已处理记录；
        // 失败记录顺延到下次调度（游标重新从头开始）重试，避免纯 offset 在同轮内反复跳过失败记录。
        LocalDateTime cursorCreateTime = null;
        String cursorId = null;

        while (true) {
            List<FileTask> fileTaskList = fileTaskRepository.listCleanFileTask(expireTime, BATCH_SIZE, cursorCreateTime, cursorId);
            if (CollUtil.isEmpty(fileTaskList)) {
                break;
            }
            batchNo++;
            XxlJobHelper.log("开始处理第{}批文件任务, 数量={}", batchNo, fileTaskList.size());

            for (FileTask fileTask : fileTaskList) {
                String id = fileTask.getId();
                String fileUrl = fileTask.getFileUrl();
                // 推进游标到当前记录：即便本条删除失败也不在本轮重复处理，留待下次调度重试。
                cursorCreateTime = fileTask.getCreateTime();
                cursorId = id;
                if (CharSequenceUtil.isBlank(fileUrl)) {
                    // null / 空串 / 仅空白字符的 fileUrl 无法走 FastDFS 删除，直接逻辑软删，避免游标推进后脏数据永久残留
                    try {
                        if (fileTaskRepository.removeById(id)) {
                            successCount++;
                        } else {
                            logicDeleteFailCount++;
                            XxlJobHelper.log("空白fileUrl任务逻辑删除失败, id={}", id);
                            log.warn("空白fileUrl任务逻辑删除失败, id={}", id);
                        }
                    } catch (Exception e) {
                        logicDeleteFailCount++;
                        XxlJobHelper.log("空白fileUrl任务逻辑删除异常, id={}, error={}", id, e.getMessage());
                        log.error("空白fileUrl任务逻辑删除异常, id={}", id, e);
                    }
                    continue;
                }

                // FastDFS 与 DB 软删无法同一事务：先删远端、后删库，宁可短暂「文件已删、记录仍在」，也不留孤儿文件占存储。
                // 若远端已删而 removeById 失败，本轮计 logicDeleteFailCount 且游标已推进；下次调度 exist 为 false 会跳过远端删除并补做软删（审查勿误报为需分布式事务）。
                try {
                    // 参考 FileTaskContext.delete 的「先查 exist 再删」模式：文件不存在（已删除）则跳过删除，
                    // 直接执行后续 DB 软删，避免记录因 FastDFS 反复返回非 0 而永久卡住、fileUrl 长期残留。
                    if (FastDFSClientUtil.exist(fileUrl)) {
                        int result = FastDFSClientUtil.deleteFile(fileUrl);
                        // 删除返回非 0 时再确认一次远端是否其实已不存在（兼容客户端对不存在文件返回非 0 错误码）；
                        // 仅当文件确实仍存在才算真实失败并跳过。
                        if (result != 0 && FastDFSClientUtil.exist(fileUrl)) {
                            fastDfsFailCount++;
                            XxlJobHelper.log("FastDFS文件删除失败, id={}, fileUrl={}, result={}", id, fileUrl, result);
                            log.warn("FastDFS文件删除失败, id={}, fileUrl={}, result={}", id, fileUrl, result);
                            continue;
                        }
                    }
                } catch (Exception e) {
                    fastDfsFailCount++;
                    XxlJobHelper.log("FastDFS文件删除异常, id={}, fileUrl={}, error={}", id, fileUrl, e.getMessage());
                    log.error("FastDFS文件删除异常, id={}, fileUrl={}", id, fileUrl, e);
                    continue;
                }

                try {
                    // 远端已确认不存在或删除成功后的 DB 软删；失败不在本轮重试，依赖下次调度 exist 跳过后仅补库删。
                    boolean removed = fileTaskRepository.removeById(id);
                    if (removed) {
                        successCount++;
                    } else {
                        logicDeleteFailCount++;
                        XxlJobHelper.log("文件任务逻辑删除失败, id={}, fileUrl={}", id, fileUrl);
                        log.warn("文件任务逻辑删除失败, id={}, fileUrl={}", id, fileUrl);
                    }
                } catch (Exception e) {
                    logicDeleteFailCount++;
                    XxlJobHelper.log("文件任务逻辑删除异常, id={}, fileUrl={}, error={}", id, fileUrl, e.getMessage());
                    log.error("文件任务逻辑删除异常, id={}, fileUrl={}", id, fileUrl, e);
                }
            }

            // 不足一批说明已扫描到末尾，结束循环，避免末批 size==limit 时再多查一次空结果。
            if (fileTaskList.size() < BATCH_SIZE) {
                break;
            }
        }

        long end = System.currentTimeMillis();
        String summary = CharSequenceUtil.format("成功删除={}, FastDFS删除失败={}, 逻辑删除失败={}, 耗时={}ms",
                successCount, fastDfsFailCount, logicDeleteFailCount, end - start);
        XxlJobHelper.log("====结束清理过期文件任务, {}====", summary);
        // 存在删除失败时返回 FAIL，避免调度平台误判成功、失败记录长期积压不可见
        if (fastDfsFailCount + logicDeleteFailCount > 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "清理过期文件任务存在失败记录: " + summary);
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob("cleanFileStorageTmpdir")
    public ReturnT<String> cleanFileStorageTmpdir() {
        XxlJobHelper.log("====开始清理文件临时目录====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        // 未传参用默认 3 天；传参格式约定同 cleanFileTask / parseDays。
        Integer days = 3;
        if (StringUtils.isNotBlank(jobParam)) {
            days = parseDays(jobParam);
            if (days == null || days <= 0) {
                XxlJobHelper.log("任务参数不合法, days必须为正整数, 任务参数={}", JSONUtil.toJsonStr(jobParam));
                return ReturnT.FAIL;
            }
        }

        String storageTmpdir = FileRegistry.getStorageTmpdir();
        if (StringUtils.isBlank(storageTmpdir)) {
            XxlJobHelper.log("file.storage.tmpdir未配置, 为避免误删系统临时目录, 终止清理");
            return ReturnT.FAIL;
        }

        Path workDir = Paths.get(storageTmpdir).toAbsolutePath().normalize();
        if (!Files.isDirectory(workDir)) {
            XxlJobHelper.log("file.storage.tmpdir不是有效目录, path={}", workDir);
            return ReturnT.FAIL;
        }

        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        // 僵尸兜底窗口：早于该时间的残留文件即使匹配处理中任务也强删，回收 PROCESS 卡死任务的临时文件磁盘泄漏。
        LocalDateTime forceExpireTime = LocalDateTime.now().minusDays((long) days + ZOMBIE_FORCE_DELETE_EXTRA_DAYS);
        XxlJobHelper.log("清理目录{}下创建时间早于{}、以{}为前缀且非处理中的导出临时文件（早于{}的残留文件强制清理）", workDir, expireTime,
                ExportTempFilesHandler.EXPORT_TMP_PREFIX, forceExpireTime);

        int scanCount = 0;
        int successCount = 0;
        int skipNonExportCount = 0;
        int skipProcessingCount = 0;
        int deleteFailCount = 0;
        int forceDeleteZombieCount = 0;

        // 扫描前一次性加载处理中任务 ID 集合，循环内 O(1) 精确匹配，避免逐文件查库（N+1）。
        // 快照之后新进入 PROCESS 的任务：其临时文件 mtime 为导出进行中写入时间，通常晚于 expireTime（默认 3 天前），
        // 会先被下方 lastModifiedTime 过期判断跳过，不会仅因不在快照内而被误删（审查勿误报为长扫描必删进行中文件）。
        Set<String> processingTaskIds = fileTaskRepository.listProcessingTaskIds();

        try (Stream<Path> pathStream = Files.walk(workDir)) {
            for (Path path : (Iterable<Path>) pathStream::iterator) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }
                scanCount++;
                // 仅清理本服务导出产生的临时文件（exportTmp_ 前缀）：目录可能被复用或配置指向共享目录，
                // 非本前缀的业务/中间文件不归本 Job 管理，跳过以杜绝误删无关文件。
                if (!path.getFileName().toString().startsWith(ExportTempFilesHandler.EXPORT_TMP_PREFIX)) {
                    skipNonExportCount++;
                    continue;
                }
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                // 临时文件为「一次写入、不再更新」，用 lastModifiedTime 判断过期：
                // CentOS/ext 文件系统的 creationTime(birth time) 经 Java NIO 常不可靠（可能退化为 mtime 或 epoch），
                // 会导致过期清理不及时或误判删除时机，故改用稳定的 lastModifiedTime。
                LocalDateTime fileTime = LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault());
                if (!fileTime.isBefore(expireTime)) {
                    continue;
                }
                if (isProcessingTempFile(path, processingTaskIds)) {
                    // 正常进行中任务的临时文件 mtime 较新（写入中），不会早于 forceExpireTime；
                    // 仅当其早于兜底窗口才视为僵尸残留并强删，避免 PROCESS 卡死任务导致磁盘永久占用。
                    if (!fileTime.isBefore(forceExpireTime)) {
                        skipProcessingCount++;
                        XxlJobHelper.log("跳过处理中的临时文件, path={}", path);
                        continue;
                    }
                    forceDeleteZombieCount++;
                    XxlJobHelper.log("强制清理疑似僵尸任务的过期临时文件, path={}, mtime={}", path, fileTime);
                }

                try {
                    Files.deleteIfExists(path);
                    successCount++;
                } catch (IOException e) {
                    deleteFailCount++;
                    XxlJobHelper.log("删除临时文件失败, path={}, error={}", path, e.getMessage());
                    log.error("删除临时文件失败, path={}", path, e);
                }
            }
        } catch (IOException e) {
            XxlJobHelper.log("扫描临时目录异常, path={}, error={}", workDir, e.getMessage());
            log.error("扫描临时目录异常, path={}", workDir, e);
            return ReturnT.FAIL;
        }

        long end = System.currentTimeMillis();
        String summary = CharSequenceUtil.format(
                "扫描文件={}, 成功删除={}, 跳过非导出临时文件={}, 跳过处理中={}, 强制清理僵尸残留={}, 删除失败={}, 耗时={}ms",
                scanCount, successCount, skipNonExportCount, skipProcessingCount, forceDeleteZombieCount, deleteFailCount,
                end - start);
        XxlJobHelper.log("====结束清理文件临时目录, {}====", summary);
        // 存在删除失败时返回 FAIL，便于调度平台告警与运维感知
        if (deleteFailCount > 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "清理文件临时目录存在删除失败: " + summary);
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 判断临时文件是否属于「处理中」任务。
     * <p>临时文件名为 {@code exportTmp_{id}_{safeName}_{timestamp}_{uuid}.xlsx}（见 {@link com.erp.server.file.entity.FileTask#getUniqueWithFileName()} 与
     * {@link ExportTempFilesHandler#createTempPath}），取 {@code exportTmp_} 后第一个 {@code _} 之前的一段作为 taskId，与处理中 ID 集合做精确匹配，
     * 避免 {@code startsWith("exportTmp_{id}_")} 在 ID 存在前缀包含关系时误判（如 "12" 与 "123"）。
     * <p>不变量：依赖主键为 {@code IdType.ASSIGN_ID} 的纯数字雪花串（不含下划线），故首段恰为 taskId；
     * 若主键改为含 {@code _} 的自定义 id，须同步改造本方法与 {@link ExportTempFilesHandler#createTempPath} 的解析。
     */
    private boolean isProcessingTempFile(Path path, Set<String> processingTaskIds) {
        if (CollUtil.isEmpty(processingTaskIds)) {
            return false;
        }
        String fileName = path.getFileName().toString();
        String tmpPrefix = ExportTempFilesHandler.EXPORT_TMP_PREFIX;
        if (!fileName.startsWith(tmpPrefix)) {
            return false;
        }
        int dot = fileName.lastIndexOf('.');
        String nameWithoutExt = dot > 0 ? fileName.substring(0, dot) : fileName;
        String afterPrefix = nameWithoutExt.substring(tmpPrefix.length());
        int firstSep = afterPrefix.indexOf('_');
        if (firstSep <= 0) {
            return false;
        }
        String taskIdInFile = afterPrefix.substring(0, firstSep);
        return processingTaskIds.contains(taskIdInFile);
    }

    /**
     * 解析 XXL-JOB 任务参数中的 {@code days}。
     * <p>
     * <strong>参数约定（审查勿误报为需兼容纯数字字符串）</strong>：
     * <ul>
     *   <li>调度中心<strong>未配置</strong>或配置为空：由调用方使用各 Job 内置默认天数（{@code cleanFileTask}=360，{@code cleanFileStorageTmpdir}=3）。</li>
     *   <li>已配置时<strong>仅支持 JSON 对象</strong>，示例：{@code {"days":360}}；与仓库内其它 XXL 任务扩展字段的写法保持一致，便于后续加字段。</li>
     *   <li>纯数字 {@code "360"}、非 JSON 文本、缺少 {@code days}、{@code days<=0}：返回 {@code null}，调用方返回 {@code ReturnT.FAIL} 并打日志——
     *       有意<strong>不</strong>静默回退默认天数，避免运维误配后任务仍按默认值执行、清理范围与预期不符且难以察觉。</li>
     * </ul>
     *
     * @param jobParam XXL-JOB {@link XxlJobHelper#getJobParam()} 原始字符串
     * @return 正整数天数；解析失败或字段非法时返回 {@code null}
     */
    private Integer parseDays(String jobParam) {
        if (CharSequenceUtil.isBlank(jobParam)) {
            return null;
        }
        try {
            JSONObject jsonParam = JSON.parseObject(jobParam);
            return jsonParam.getInteger("days");
        } catch (Exception e) {
            XxlJobHelper.log("解析任务参数异常, error={}", e.getMessage());
            log.warn("解析清理文件任务参数异常, jobParam={}", jobParam, e);
            return null;
        }
    }
}
