package com.erp.server.file.core;

import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.databind.JavaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 多 sheet 导出公共样板：
 * 参数解析、临时文件、上传与清理统一在父类实现，子类仅负责分页取数与写入策略。
 */
@Slf4j
public abstract class AbstractMultiSheetPageFileEventHandler<P> extends AbstractFileEventHandler<Object> {

    @Override
    public final void handle(FileTask fileTask) {
        Path tempPath = null;
        try {
            P params = resolveExportParams(fileTask);
            String excelPath = getExcelPath(params);
            tempPath = ExportTempFilesHandler.createTempPath(FileRegistry.getStorageTmpdir(), ".xlsx", fileTask.getUniqueWithFileName());
            int total = writeAllSheets(tempPath.toFile(), params, excelPath);
            fileTask.setCount(total);
            String displayName = buildDownloadFileName(fileTask, excelPath);
            fileTask.setFileUrl(FastDFSClientUtil.uploadFile(tempPath.toFile(), displayName, null));
        } catch (IOException e) {
            log.error("多sheet导出失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            ExportTempFilesHandler.deleteQuietly(tempPath);
        }
    }

    /**
     * 由策略子类实现具体写入路径，返回主 sheet 行数（用于 fileTask.count）。
     */
    protected abstract int writeAllSheets(File outFile, P params, String excelPath) throws IOException;

    /**
     * 按导出参数选择模板。
     */
    protected abstract String getExcelPath(P params);

    protected MultiSheetTemplateWriter newWriter(String excelPath) {
        return new MultiSheetTemplateWriter(
                excelPath,
                maxDataRowsPerSheet(),
                maxTemplateDataSheets(),
                maxRowsPerXlsxSheetHardLimit(),
                getWriteHandler());
    }

    @SuppressWarnings("unchecked")
    protected P resolveExportParams(FileTask fileTask) {
        Class<?> paramType = resolveExportParamType(getClass());
        if (paramType == null) {
            throw new IllegalStateException(getClass().getName()
                    + " 无法推断导出参数类型 P，请确保直接继承 AbstractMultiSheetPageFileEventHandler<P> 并指定具体 P，或重写 resolveExportParams");
        }
        JavaType javaType = getObjectMapper().getTypeFactory().constructType(paramType);
        Object value = readValue(fileTask.getMetaInfo(), javaType);
        return (P) value;
    }

    static Class<?> resolveExportParamType(Class<?> handlerClass) {
        ResolvableType param = ResolvableType
                .forClass(AbstractMultiSheetPageFileEventHandler.class, handlerClass)
                .getGeneric(0);
        return param.resolve();
    }

    protected int getPageSize() {
        return 5000;
    }

    protected int getFirstPage() {
        return 1;
    }

    protected int reservedTemplateHeaderRows() {
        return 0;
    }

    protected int maxDataRowsPerSheet() {
        return Math.max(1, FileRegistry.getSheetMaxRows() - reservedTemplateHeaderRows());
    }

    protected int maxTemplateDataSheets() {
        return FileRegistry.getMaxSheetNum();
    }

    protected int maxRowsPerXlsxSheetHardLimit() {
        return Math.max(1, 1_048_576 - reservedTemplateHeaderRows() - 1);
    }

    @Override
    protected final List<Object> getData(FileTask fileTask) {
        throw new UnsupportedOperationException("多sheet导出请使用 handle(FileTask)");
    }

    @Override
    public final String getExcelPath() {
        throw new UnsupportedOperationException("多sheet导出请使用 getExcelPath(P)");
    }
}
