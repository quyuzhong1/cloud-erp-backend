package com.erp.server.file.core.dynamic;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.server.file.core.AbstractSingleSheetGroupPageFileEventHandler;
import com.erp.server.file.handler.FileRegistry;

/**
 * 动态表头「单数据 sheet」导出（与 {@link AbstractSingleSheetGroupPageFileEventHandler} 对称）：列由运行时表头算出（无固定模板），
 * 强制只写 1 张数据 sheet，行数上限由 {@code file.storage.singleSheetMaxRows} 配置（默认 Excel2007 物理上限 1048576，已预留表头行）。
 * <p>
 * <strong>主要用于按 sheet 做分组判重/置空的动态表头导出</strong>（如销售订单 {@code ExportOmsSoHandler}：用
 * {@link #newSheetState()} + {@link #decorateSheetRow} 在单 sheet 内对主单字段判重置空）。这类场景必须强制单 sheet，否则：
 * <ul>
 *   <li>数据换 sheet 后 {@code sheetState} 复位，同一分组跨 sheet 边界会重复展示主单字段；</li>
 *   <li>产生不可预估的 sheet 数量。</li>
 * </ul>
 * 达到单 sheet 上限即显式失败，请缩小筛选范围；数据量可能超过单 sheet 容量、且无按 sheet 分组诉求时，
 * 请改用多 sheet 的 {@link AbstractDynamicHeadersFileEventHandler}。
 * <p>
 * 子类实现 {@link #getPageData(com.common.business.dto.base.PagingDTO)} 即可；可选重写 {@link #firstRowName()}、
 * {@link #dynamicHeaderCellStyleStrategy()}、{@link #newSheetState()} / {@link #decorateSheetRow} 等钩子。
 *
 * @param <P> 查询参数类型
 * @see AbstractDynamicHeadersFileEventHandler
 * @see AbstractSingleSheetGroupPageFileEventHandler
 */
public abstract class AbstractSingleSheetDynamicHeadersFileEventHandler<P> extends AbstractDynamicHeadersFileEventHandler<P> {

    /**
     * 固定单数据 sheet：达到 {@link #maxRowsPerSheet()} 即显式失败、不再开新 sheet。
     */
    @Override
    protected int maxSheetCount() {
        return 1;
    }

    /**
     * 单 sheet 数据行上限取 {@code file.storage.singleSheetMaxRows}，并按实际表头行数预留：
     * 列名行恒占 1 行，重写 {@link #firstRowName()} 返回非空时再额外占 1 行合并标题行。
     */
    @Override
    protected int maxRowsPerSheet() {
        int reservedHeaderRows = CharSequenceUtil.isNotBlank(firstRowName()) ? 2 : 1;
        return Math.max(1, FileRegistry.singleSheetMaxRowsOrDefault() - reservedHeaderRows);
    }
}
