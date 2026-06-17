package com.erp.server.file.core;

import com.common.core.exception.ServiceException;
import com.erp.server.file.handler.FileRegistry;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * classpath 导出模板读取：读入前/后按 {@link FileRegistry#maxSingleTemplateBytesOrDefault()} 早失败，
 * 避免超大 xlsx 整文件进堆后再做 POI 展开。
 */
public final class ClasspathExportTemplateReader {

    private ClasspathExportTemplateReader() {
    }

    /**
     * 读取 classpath 模板为字节数组。
     * <p>
     * 单份体积上界 = {@code maxTemplateExpandBytes / maxSheetNum}（最坏展开满 sheet 时不超足迹配置）。
     */
    public static byte[] readBytes(String excelPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(excelPath);
        long maxSingle = FileRegistry.maxSingleTemplateBytesOrDefault();
        long contentLength = resource.contentLength();
        if (contentLength >= 0) {
            assertWithinSingleTemplateLimit(excelPath, contentLength, maxSingle);
        }
        try (InputStream in = resource.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(in);
            assertWithinSingleTemplateLimit(excelPath, bytes.length, maxSingle);
            return bytes;
        }
    }

    private static void assertWithinSingleTemplateLimit(String excelPath, long actualBytes, long maxSingle) {
        if (actualBytes > maxSingle) {
            throw new ServiceException("导出模板体积过大（模板=" + excelPath + "，约 "
                    + (actualBytes / 1024 / 1024) + "MB，单份上限约 " + (maxSingle / 1024 / 1024)
                    + "MB，由 file.storage.maxTemplateExpandBytes / file.storage.maxSheetNum 推导），"
                    + "请简化模板或联系管理员调整配置。");
        }
    }
}
