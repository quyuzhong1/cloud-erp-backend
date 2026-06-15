package com.erp.server.file.core.multisheet;

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
     * 各 sheet 行提取器（顺序须与模板 sheet 一致；index 0 通常为主表透传）。
     * <p>
     * {@code params} 由 {@link #sheetSpec(P)} 传入本方法签名，但<strong>不</strong>转交 {@link #buildSheetExtractors()}：
     * 筛选条件已在 {@link #fetchMasterPage(PagingDTO)} 的 {@code dto.getParams()} 中参与远端分页查询；
     * extractor 仅对主分页切片做内存派生（主行 → 各 sheet 行）。子类若需按 P 定制提取逻辑，应重写 {@link #sheetSpec(P)}
     * 或在 extractor 闭包中自行捕获 params（审查勿误报为 params 被丢弃导致无法实现）。
     */
    protected final List<Function<List<M>, List<?>>> sheetExtractors(P params) {
        return buildSheetExtractors();
    }

    /**
     * 子类只需提供主分页查询实现；导出筛选参数经 {@link PagingDTO#getParams()} 传入 Feign/查询。
     */
    protected abstract PagingVO<M> fetchMasterPage(PagingDTO<P> dto);

    /**
     * 子类提供各 sheet 提取器；无参因筛选已在 {@link #fetchMasterPage}，此处仅做主行 → 明细行映射。
     */
    protected abstract List<Function<List<M>, List<?>>> buildSheetExtractors();
}
