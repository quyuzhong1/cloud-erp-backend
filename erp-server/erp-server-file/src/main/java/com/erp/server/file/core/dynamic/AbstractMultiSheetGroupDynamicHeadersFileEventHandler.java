package com.erp.server.file.core.dynamic;

/**
 * 动态表头「多 sheet 分组」导出（与 {@link com.erp.server.file.core.AbstractMultiSheetGroupPageFileEventHandler} 对称）：
 * 列由运行时表头算出（无固定模板），按 {@code file.storage.sheetMaxRows}（默认 10 万行）多 tab 分页，
 * 同一业务分组不拆 sheet，临界点整组回退到下一 sheet。
 * <p>
 * 导出开始前会按 {@code ceil(totalCount / sheetMaxRows) + sheetGroupExtraSheetCount()} 预估所需 sheet 数并早失败；
 * 极端分组形态下临界点回退仍可能多占 sheet，接近 {@code file.storage.maxSheetNum} 上限时请缩小筛选范围。
 * <p>
 * <strong>何时选用本类（方案 C）</strong>
 * <ul>
 *   <li>动态表头导出，数据量超过单 sheet 容量，必须多 sheet 分页</li>
 *   <li>主从/分组展示：组内首行保留主表字段、后续行置空（见 {@link #beforeWriteGroupRows(Object, java.util.List)}）</li>
 * </ul>
 * <strong>何时选用 {@link AbstractSingleSheetDynamicHeadersFileEventHandler}（方案 B）</strong>
 * <ul>
 *   <li>数据量可落在单 sheet 内，希望固定 1 张数据 sheet</li>
 * </ul>
 * <strong>何时选用 {@link AbstractDynamicHeadersFileEventHandler}（无分组）</strong>
 * <ul>
 *   <li>普通扁平动态表头列表，无需「同组不拆 sheet」</li>
 * </ul>
 * <p>
 * 子类须实现 {@link #getPageData(com.common.business.dto.base.PagingDTO)}，并保证查询结果按
 * {@link #sheetGroupKey(java.util.LinkedHashMap)} 稳定连续排序；通常还需重写 {@link #sheetGroupKey}、
 * {@link #beforeWriteGroupRows}，展示强依赖连续分组时可设 {@link #failOnNonContinuousSheetGroup()} 为 {@code true}。
 *
 * @param <P> 查询参数类型
 * @see AbstractDynamicHeadersFileEventHandler
 * @see AbstractSingleSheetDynamicHeadersFileEventHandler
 * @see com.erp.server.file.core.AbstractMultiSheetGroupPageFileEventHandler
 */
public abstract class AbstractMultiSheetGroupDynamicHeadersFileEventHandler<P> extends AbstractDynamicHeadersFileEventHandler<P> {

    /**
     * 启用同组不拆 sheet；临界点整组回退到下一 sheet。
     */
    @Override
    protected boolean keepSheetGroupTogether() {
        return true;
    }

    /**
     * 分组临界点回退时计入预检的 sheet 加成数（见 {@link AbstractDynamicHeadersFileEventHandler#sheetGroupExtraSheetCount()}）。
     */
    @Override
    protected int sheetGroupExtraSheetCount() {
        return 1;
    }
}
