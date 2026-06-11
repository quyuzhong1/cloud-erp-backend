package com.erp.server.file.core.multisheet;

import com.erp.server.file.core.AbstractFileEventHandler;
import com.erp.server.file.core.ExportTempFilesHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.databind.JavaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 多 sheet 导出公共样板：
 * 参数解析、临时文件、上传与清理统一在父类实现，子类仅负责分页取数与写入策略。
 */
@Slf4j
public abstract class AbstractMultiSheetPageFileEventHandler<P> extends AbstractFileEventHandler<Object> {

    @Override
    public final void handle(FileTask fileTask) {
        P params = resolveExportParams(fileTask);
        String excelPath = getExcelPath(params);
        String displayName = buildDownloadFileName(fileTask, excelPath);
        // 复用统一导出模板：流式上传（streamUploadFile），避免整文件入内存导致大文件 OOM 与上传失败被静默写入空 url
        ExportTempFilesHandler.exportToTempAndUpload(fileTask, ".xlsx", displayName,
                outFile -> writeAllSheets(outFile, params, excelPath));
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
        Type paramType = resolveExportParamType(getClass());
        if (paramType == null) {
            throw new IllegalStateException(getClass().getName()
                    + " 无法推断导出参数类型 P，请确保直接继承 AbstractMultiSheetPageFileEventHandler<P> 并指定具体 P，或重写 resolveExportParams");
        }
        JavaType javaType = getObjectMapper().getTypeFactory().constructType(paramType);
        Object value = readValue(fileTask.getMetaInfo(), javaType);
        return (P) value;
    }

    /**
     * 从当前 Handler 类沿继承链解析 {@link AbstractMultiSheetPageFileEventHandler} 的类型参数 P，
     * 返回 {@link Type} 以保留泛型嵌套信息（与 {@code AbstractPageFileEventHandler} 的解析方式一致）。
     */
    static Type resolveExportParamType(Class<?> handlerClass) {
        ResolvableType rt = ResolvableType.forClass(handlerClass);
        while (rt != ResolvableType.NONE) {
            if (rt.getRawClass() != null && rt.getRawClass() == AbstractMultiSheetPageFileEventHandler.class) {
                ResolvableType param = rt.getGeneric(0);
                if (param != ResolvableType.NONE && null != param.getType()) {
                    return param.getType();
                }
                return null;
            }
            rt = rt.getSuperType();
        }
        return null;
    }

    protected int getPageSize() {
        return FileRegistry.exportPageSize();
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
