package com.erp.server.file.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.FileTaskStatusEnum;
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
import java.util.ArrayList;
import java.util.List;
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
        List<String> skipTaskIds = new ArrayList<>();

        while (true) {
            List<FileTask> fileTaskList = fileTaskRepository.listCleanFileTask(expireTime, BATCH_SIZE, skipTaskIds);
            if (CollUtil.isEmpty(fileTaskList)) {
                break;
            }
            batchNo++;
            XxlJobHelper.log("开始处理第{}批文件任务, 数量={}", batchNo, fileTaskList.size());

            for (FileTask fileTask : fileTaskList) {
                String id = fileTask.getId();
                String fileUrl = fileTask.getFileUrl();
                if (CharSequenceUtil.isBlank(fileUrl)) {
                    skipTaskIds.add(id);
                    continue;
                }

                try {
                    int result = FastDFSClientUtil.deleteFile(fileUrl);
                    if (result != 0) {
                        fastDfsFailCount++;
                        skipTaskIds.add(id);
                        XxlJobHelper.log("FastDFS文件删除失败, id={}, fileUrl={}, result={}", id, fileUrl, result);
                        log.warn("FastDFS文件删除失败, id={}, fileUrl={}, result={}", id, fileUrl, result);
                        continue;
                    }
                } catch (Exception e) {
                    fastDfsFailCount++;
                    skipTaskIds.add(id);
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
                        skipTaskIds.add(id);
                        XxlJobHelper.log("文件任务逻辑删除失败, id={}, fileUrl={}", id, fileUrl);
                        log.warn("文件任务逻辑删除失败, id={}, fileUrl={}", id, fileUrl);
                    }
                } catch (Exception e) {
                    logicDeleteFailCount++;
                    skipTaskIds.add(id);
                    XxlJobHelper.log("文件任务逻辑删除异常, id={}, fileUrl={}, error={}", id, fileUrl, e.getMessage());
                    log.error("文件任务逻辑删除异常, id={}, fileUrl={}", id, fileUrl, e);
                }
            }
        }

        long end = System.currentTimeMillis();
        XxlJobHelper.log("====结束清理过期文件任务, 成功删除={}, FastDFS删除失败={}, 逻辑删除失败={}, 耗时={}ms====",
                successCount, fastDfsFailCount, logicDeleteFailCount, end - start);
        return ReturnT.SUCCESS;
    }

    @XxlJob("cleanFileStorageTmpdir")
    public ReturnT<String> cleanFileStorageTmpdir() {
        XxlJobHelper.log("====开始清理文件临时目录====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        Integer days = 3;
        if (StringUtils.isBlank(jobParam)) {
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
        XxlJobHelper.log("清理目录{}下创建时间早于{}且非处理中的文件", workDir, expireTime);

        int scanCount = 0;
        int successCount = 0;
        int skipProcessingCount = 0;
        int deleteFailCount = 0;

        try (Stream<Path> pathStream = Files.walk(workDir)) {
            for (Path path : (Iterable<Path>) pathStream::iterator) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }
                scanCount++;
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                LocalDateTime createTime = LocalDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());
                if (!createTime.isBefore(expireTime)) {
                    continue;
                }
                if (isProcessingTempFile(path)) {
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
        XxlJobHelper.log("====结束清理文件临时目录, 扫描文件={}, 成功删除={}, 跳过处理中={}, 删除失败={}, 耗时={}ms====",
                scanCount, successCount, skipProcessingCount, deleteFailCount, end - start);
        return ReturnT.SUCCESS;
    }

    private boolean isProcessingTempFile(Path path) {
        String fileName = path.getFileName().toString();
        if (!fileName.startsWith(ExportTempFilesHandler.EXPORT_TMP_PREFIX)) {
            return false;
        }
        String taskId = parseTaskId(fileName);
        if (CharSequenceUtil.isBlank(taskId)) {
            return false;
        }
        return fileTaskRepository.lambdaQuery()
                .eq(FileTask::getId, taskId)
                .eq(FileTask::getStatus, FileTaskStatusEnum.PROCESS.name())
                .count() > 0;
    }

    private String parseTaskId(String fileName) {
        String suffixName = fileName.substring(ExportTempFilesHandler.EXPORT_TMP_PREFIX.length());
        int endIndex = suffixName.indexOf("_");
        if (endIndex <= 0) {
            return null;
        }
        return suffixName.substring(0, endIndex);
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
