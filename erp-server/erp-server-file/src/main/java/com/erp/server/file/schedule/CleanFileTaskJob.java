package com.erp.server.file.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.core.ExportTempFilesHandler;
import com.erp.server.file.entity.FileTask;
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


    @Resource
    private FileTaskRepository fileTaskRepository;

    @XxlJob("cleanFileTask")
    public ReturnT<String> cleanFileTask() {
        XxlJobHelper.log("====开始清理过期文件任务====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        Integer days = 360;
        if (StringUtils.isNotBlank(jobParam)){
            days = parseDays(jobParam);
            if (days == null || days <= 0) {
                XxlJobHelper.log("任务参数不合法, days必须为正整数, 任务参数={}", JSONUtil.toJsonStr(jobParam));
                return ReturnT.FAIL;
            }
        }

        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        XxlJobHelper.log("清理创建时间早于{}且fileUrl不为空的文件任务", expireTime);

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
                    continue;
                }

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
        XxlJobHelper.log("清理目录{}下创建时间早于{}、以{}为前缀且非处理中的导出临时文件", workDir, expireTime,
                ExportTempFilesHandler.EXPORT_TMP_PREFIX);

        int scanCount = 0;
        int successCount = 0;
        int skipNonExportCount = 0;
        int skipProcessingCount = 0;
        int deleteFailCount = 0;

        // 扫描前一次性加载处理中任务ID，循环内 O(1) 判断，避免逐文件查库（N+1）。
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
                    skipProcessingCount++;
                    XxlJobHelper.log("跳过处理中的临时文件, path={}", path);
                    continue;
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
        String summary = CharSequenceUtil.format("扫描文件={}, 成功删除={}, 跳过非导出临时文件={}, 跳过处理中={}, 删除失败={}, 耗时={}ms",
                scanCount, successCount, skipNonExportCount, skipProcessingCount, deleteFailCount, end - start);
        XxlJobHelper.log("====结束清理文件临时目录, {}====", summary);
        // 存在删除失败时返回 FAIL，便于调度平台告警与运维感知
        if (deleteFailCount > 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "清理文件临时目录存在删除失败: " + summary);
        }
        return ReturnT.SUCCESS;
    }

    private boolean isProcessingTempFile(Path path, Set<String> processingTaskIds) {
        if (CollUtil.isEmpty(processingTaskIds)) {
            return false;
        }
        String fileName = path.getFileName().toString();
        if (!fileName.startsWith(ExportTempFilesHandler.EXPORT_TMP_PREFIX)) {
            return false;
        }
        // 不从文件名反推 taskId（避免依赖「id 不含下划线」的隐含约定：净化后的文件名可能含多个下划线段）。
        // 改为用处理中任务ID集合正向构造前缀 exportTmp_{id}_ 匹配，无论 id 内部含何字符都判定准确。
        for (String taskId : processingTaskIds) {
            if (CharSequenceUtil.isBlank(taskId)) {
                continue;
            }
            String expectedPrefix = ExportTempFilesHandler.EXPORT_TMP_PREFIX + taskId + "_";
            if (fileName.startsWith(expectedPrefix)) {
                return true;
            }
        }
        return false;
    }

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
