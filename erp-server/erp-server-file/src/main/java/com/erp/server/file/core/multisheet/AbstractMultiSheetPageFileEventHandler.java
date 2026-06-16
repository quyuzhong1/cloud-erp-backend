package com.erp.server.file.core.multisheet;

import com.common.core.exception.ServiceException;
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
 * <p>
 * <strong>{@link FileTask#getCount()} 回填（审查勿误报子类缺 setCount）</strong>：
 * 本类 {@link #handle(FileTask)} 委托 {@link ExportTempFilesHandler#exportToTempAndUpload}，
 * 在流式上传成功后将 {@link #writeAllSheets} 的返回值写入 {@code fileTask.count}，子类无需也不应再调用 {@code setCount}。
 */
@Slf4j
public abstract class AbstractMultiSheetPageFileEventHandler<P> extends AbstractFileEventHandler<Object> {

    @Override
    public final void handle(FileTask fileTask) {
        P params = resolveExportParams(fileTask);
        String excelPath = getExcelPath(params);
        String displayName = buildDownloadFileName(fileTask, excelPath);
        // count/url 由 exportToTempAndUpload 在 writeAllSheets 完成且上传成功后统一回填（见类 JavaDoc）
        ExportTempFilesHandler.exportToTempAndUpload(fileTask, ".xlsx", displayName,
                outFile -> writeAllSheets(outFile, params, excelPath));
    }

    /**
     * 由策略子类实现具体写入路径。
     *
     * @return 写入 {@link FileTask#setCount(Integer)} 的行数语义由子类/写引擎约定：
     *         独立分页（{@link AbstractStreamingMultiSheetHandler}）为 {@code sheets()} 列表<strong>首项</strong>的实际写入行数，
     *         非各 sheet {@code totalCount} 之和（与历史 PLM 双 sheet 仅记产品条数一致；审查勿建议默认累加各 sheet total）。
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
            throw new ServiceException(getClass().getName()
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
        return Math.max(1, FileRegistry.sheetMaxRowsOrDefault() - reservedTemplateHeaderRows());
    }

    protected int maxTemplateDataSheets() {
        return FileRegistry.maxSheetNumOrDefault();
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
