package com.erp.server.file.core;

import com.erp.server.file.handler.FileRegistry;

/**
 * 分组报表型分页导出（方案 B）：强制单数据 sheet，行数上限由 {@code file.storage.singleSheetMaxRows} 配置（默认 200000，可配至 Excel 物理上限 1048576）。
 * <p>
 * 适用主从分组、数据量在单 sheet 容量内的展示型导出（如 SKU+实体仓 / 虚拟仓明细）。固定只展开 1 张数据 sheet、
 * {@link #sheetGroupExtraSheetCount()} 为 0，无尾部空 tab。
 * <p>
 * 数据量超过单 sheet 容量时请改用 {@link AbstractMultiSheetGroupPageFileEventHandler}（方案 C：多 sheet + 可接受尾部空 tab）。
 * <p>
 * 子类须：
 * <ul>
 *   <li>重写 {@link #sheetGroupKey(Object)}，并保证上游按该 key 连续排序</li>
 *   <li>在 {@link #beforeWriteGroupRows(Object, java.util.List)} 或 {@link #beforeWriteRows(java.util.List)} 中处理组内展示逻辑</li>
 * </ul>
 * 数据量超过 {@link #maxDataRowsPerSheet()} 时将显式失败，请缩小筛选范围。
 *
 * @param <T> 导出行类型
 * @param <P> 查询参数类型
 * @see AbstractMultiSheetGroupPageFileEventHandler
 */
public abstract class AbstractSingleSheetGroupPageFileEventHandler<T, P> extends AbstractPageFileEventHandler<T, P> {

    @Override
    protected boolean preferSingleDataSheetForGroupKeeping() {
        return true;
    }

    @Override
    protected boolean keepSheetGroupTogether() {
        return true;
    }

    @Override
    protected int maxDataRowsPerSheet() {
        int configuredCap = Math.max(1, FileRegistry.singleSheetMaxRowsOrDefault() - reservedTemplateHeaderRows());
        return Math.min(configuredCap, maxRowsPerXlsxSheetHardLimit());
    }

    @Override
    protected int sheetGroupExtraSheetCount() {
        return 0;
    }
}
