package com.erp.server.file.core;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * 主从派生：一次主分页查询，按主 sheet 分段将派生数据写入对应 detail_i。
 */
public abstract class AbstractMasterDerivedSheetHandler<P, M> extends AbstractMultiSheetPageFileEventHandler<P> {

    @Override
    protected final int writeAllSheets(File outFile, P params, String excelPath) throws IOException {
        return newWriter(excelPath).streamMasterDerived(outFile, sheetSpec(params), getPageSize(), getFirstPage());
    }

    protected MultiSheetTemplateWriter.MasterDerivedSpec<P, M> sheetSpec(P params) {
        return new MultiSheetTemplateWriter.MasterDerivedSpec<>(
                params,
                masterPageFetcher(params),
                sheetExtractors(params)
        );
    }

    /**
     * 主列表分页查询（通常是一个 Feign 调用）
     */
    protected final Function<PagingDTO<P>, PagingVO<M>> masterPageFetcher(P params) {
        return this::fetchMasterPage;
    }

    /**
     * 各 sheet 提取器（顺序必须与模板 sheet 顺序一致；index 0 通常为主表本身）
     */
    protected final List<Function<List<M>, List<?>>> sheetExtractors(P params) {
        return buildSheetExtractors();
    }

    /**
     * 子类只需提供主分页查询实现。
     */
    protected abstract PagingVO<M> fetchMasterPage(PagingDTO<P> dto);

    /**
     * 子类只需提供各 sheet 提取器实现。
     */
    protected abstract List<Function<List<M>, List<?>>> buildSheetExtractors();
}
