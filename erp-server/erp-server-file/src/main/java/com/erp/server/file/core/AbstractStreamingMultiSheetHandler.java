package com.erp.server.file.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 独立分页：每个 sheet 独立分页查询并按自身 total 预克隆多张后流式写入。
 */
public abstract class AbstractStreamingMultiSheetHandler<P> extends AbstractMultiSheetPageFileEventHandler<P> {

    @Override
    protected final int writeAllSheets(File outFile, P params, String excelPath) throws IOException {
        return newWriter(excelPath).streamIndependent(outFile, sheets(params), getPageSize(), getFirstPage());
    }

    protected abstract List<MultiSheetTemplateWriter.IndependentSheet<P>> sheets(P params);
}
