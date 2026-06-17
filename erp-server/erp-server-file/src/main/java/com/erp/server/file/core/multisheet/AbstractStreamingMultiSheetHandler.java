package com.erp.server.file.core.multisheet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 独立分页：每个 sheet 独立分页查询并按自身 total 预克隆多张后流式写入。
 * <p>
 * {@link #writeAllSheets} 返回值来自 {@link MultiSheetTemplateWriter#streamIndependent}：
 * {@code sheets(params)} 列表<strong>第一项</strong>的实际写入行数（单 sheet 时即该 sheet；双 sheet 产品开发时首项为产品，与旧版仅 {@code setCount(产品条数)} 一致）。
 */
public abstract class AbstractStreamingMultiSheetHandler<P> extends AbstractMultiSheetPageFileEventHandler<P> {

    @Override
    protected final int writeAllSheets(File outFile, P params, String excelPath) throws IOException {
        return newWriter(excelPath).streamIndependent(outFile, sheets(params), getPageSize(), getFirstPage());
    }

    protected abstract List<MultiSheetTemplateWriter.IndependentSheet<P>> sheets(P params);
}
