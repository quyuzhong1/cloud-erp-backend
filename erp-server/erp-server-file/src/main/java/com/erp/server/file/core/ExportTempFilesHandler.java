package com.erp.server.file.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 导出任务临时文件
 */
@Slf4j
public final class ExportTempFilesHandler {

    private ExportTempFilesHandler() {
    }

    public static Path createTempPath(String exportFilePath, String suffix, String fileName) throws IOException {
        Path dir = resolveWorkDir(exportFilePath);
        Files.createDirectories(dir);
        String name = "exportTmp_".concat(fileName).concat(suffix);
        return dir.resolve(name);
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
