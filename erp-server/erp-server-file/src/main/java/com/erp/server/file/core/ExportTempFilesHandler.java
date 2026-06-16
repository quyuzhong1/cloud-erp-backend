package com.erp.server.file.core;

import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.entity.FileTask;
import com.common.core.exception.ServiceException;
import com.erp.server.file.handler.FileRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 导出任务临时文件
 */
@Slf4j
public final class ExportTempFilesHandler {

    public static final String EXPORT_TMP_PREFIX = "exportTmp_";

    private ExportTempFilesHandler() {
    }

    /**
     * 将 Excel 写出到临时文件的回调。实现只负责把数据写入 {@code outFile}，并返回写出的数据行数（用于 {@link FileTask#setCount}）。
     */
    @FunctionalInterface
    public interface TempFileWriter {
        int write(File outFile) throws IOException;
    }

    /**
     * 导出统一模板：创建临时文件 → 由 {@code writer} 写出 → 流式上传 FastDFS → 上传成功后再回填 {@code count} 与 {@code fileUrl} → 清理临时文件。
     * <p>
     * 统一使用 {@link FastDFSClientUtil#streamUploadFile} 流式上传（失败抛 {@code ServiceException}），
     * 避免整文件读入内存导致大文件 OOM，也避免上传失败被静默写入空 url。各导出基类务必复用本方法，
     * 不要各自再写一份上传逻辑，以防上传方式再次分叉。
     *
     * @param fileTask    导出任务，方法内回填 {@code count} 与 {@code fileUrl}
     * @param suffix      临时文件后缀，如 {@code ".xlsx"}
     * @param displayName 上传后的展示文件名
     * @param writer      写出回调，返回写出的数据行数
     */
    public static void exportToTempAndUpload(FileTask fileTask, String suffix, String displayName, TempFileWriter writer) {
        Path tempPath = null;
        try {
            tempPath = createTempPath(FileRegistry.getStorageTmpdir(), suffix, fileTask.getUniqueWithFileName());
            int total = writer.write(tempPath.toFile());
            String url = FastDFSClientUtil.streamUploadFile(tempPath.toFile(), displayName, null);
            // 上传成功后再回填 count/url，避免 FastDFS 失败时内存中的 fileTask 已带 count 被 finally 持久化到 FAIL 任务
            fileTask.setCount(total);
            fileTask.setFileUrl(url);
        } catch (IOException e) {
            log.error("导出上传失败{}", e.getMessage(), e);
            throw new ServiceException(e, "导出上传失败");
        } finally {
            deleteQuietly(tempPath);
        }
    }

    public static Path createTempPath(String exportFilePath, String suffix, String fileName) throws IOException {
        Path dir = resolveWorkDir(exportFilePath).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        // 在确定性段（id_文件名_时间戳）后追加随机后缀，避免同一任务毫秒级重复调度时临时文件互相覆盖。
        // 命名形如 exportTmp_{id}_{...}_{uuid}.xlsx；CleanFileTaskJob 仍取 exportTmp_ 后第一段为 taskId。
        String name = EXPORT_TMP_PREFIX
                .concat(sanitizeFileName(fileName))
                .concat("_")
                .concat(UUID.randomUUID().toString().replace("-", ""))
                .concat(suffix);
        // 防路径穿越：净化文件名后再二次校验最终路径必须落在工作目录内，避免 fileName 含 / 或 .. 等字符写出目录之外
        Path target = dir.resolve(name).normalize();
        if (!target.startsWith(dir)) {
            throw new IOException("非法的临时文件名，存在路径穿越风险: " + fileName);
        }
        return target;
    }

    /**
     * 净化临时文件名：仅保留 {@code [a-zA-Z0-9._-]}，其余（含 {@code / \ ..} 等路径分隔/穿越字符）替换为下划线；
     * 净化后为空或仅由点组成时回退为时间戳，确保是单段合法文件名。
     */
    private static String sanitizeFileName(String fileName) {
        String safe = StringUtils.isBlank(fileName) ? "" : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (StringUtils.isBlank(safe) || safe.replace(".", "").isEmpty()) {
            return String.valueOf(System.currentTimeMillis());
        }
        return safe;
    }

    public static Path resolveWorkDir(String exportFilePath) {
        if (StringUtils.isNotBlank(exportFilePath)) {
            return Paths.get(exportFilePath).toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    public static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除临时文件失败: {}", path, e);
        }
    }
}
