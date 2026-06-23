package com.erp.server.file.core;

/**
 * 大数据分组报表型分页导出：多 sheet 按 {@code file.storage.sheetMaxRows}（默认 10 万行）分页，
 * 同一业务分组不拆 sheet，额外预留 sheet 承接临界点回退，<strong>不做写后 trim</strong>（可接受尾部空 tab）。
 * <p>
 * <strong>何时选用本类（方案 C）</strong>
 * <ul>
 *   <li>数据量超过单 sheet 容量，必须多 sheet 分页</li>
 *   <li>主从/分组展示：组内首行保留主表字段、后续行置空或汇总（见 {@link #beforeWriteGroupRows(Object, java.util.List)}）</li>
 *   <li>可接受导出 Excel 末尾偶尔多 1 个空 sheet tab（纯展示瑕疵，不影响数据正确性）</li>
 * </ul>
 * <strong>何时选用 {@link AbstractSingleSheetGroupPageFileEventHandler}（方案 B）</strong>
 * <ul>
 *   <li>数据量可落在单 sheet 内（{@code file.storage.singleSheetMaxRows}，默认 Excel 物理上限 104 万行）</li>
 *   <li>希望固定 1 张数据 sheet、无尾部空 tab</li>
 * </ul>
 * <strong>何时选用 {@link AbstractPageFileEventHandler}（无分组）</strong>
 * <ul>
 *   <li>普通扁平列表导出，无需「同组不拆 sheet」</li>
 * </ul>
 * <p>
 * <strong>子类必须实现</strong>
 * <ul>
 *   <li>{@link #getPageData(com.common.business.dto.base.PagingDTO)} 或键集模式下 {@link #fetchKeyset(Object, Long, int)}：
 *       查询结果须按 {@link #sheetGroupKey(Object)} <strong>稳定连续排序</strong></li>
 *   <li>{@link #sheetGroupKey(Object)}：返回分组维度 key（如 {@code skuId + warehouseId}），同组行 key 相同且相邻</li>
 * </ul>
 * <strong>子类通常还需重写</strong>
 * <ul>
 *   <li>{@link #beforeWriteGroupRows(Object, java.util.List)}：组内展示逻辑（如重复主表字段置空）</li>
 *   <li>{@link #failOnNonContinuousSheetGroup()}：若展示强依赖连续分组，设为 {@code true} 快速失败</li>
 *   <li>{@link #sheetGroupExtraSheetCount()}：超大分组或频繁临界点回退时可增大预留（默认 1）</li>
 * </ul>
 * <strong>子类一般无需重写</strong>
 * <ul>
 *   <li>{@link #keepSheetGroupTogether()} — 本类已固定 {@code true}</li>
 *   <li>{@link #preferSingleDataSheetForGroupKeeping()} — 本类已固定 {@code false}</li>
 *   <li>{@link #maxDataRowsPerSheet()} — 沿用 {@code file.storage.sheetMaxRows}</li>
 * </ul>
 * <p>
 * <strong>配置项</strong>
 * <ul>
 *   <li>{@code file.storage.sheetMaxRows}：单 sheet 数据行上限（默认 100000）</li>
 *   <li>{@code file.storage.maxSheetNum}：最多数据 sheet 数（默认 50）</li>
 * </ul>
 * <p>
 * <strong>示例骨架</strong>
 * <pre>{@code
 * public class ExportXxxHandler extends AbstractMultiSheetGroupPageFileEventHandler<RowDTO, ParamDTO> {
 *
 *     @Override
 *     protected Object sheetGroupKey(RowDTO row) {
 *         return row.getMasterId();
 *     }
 *
 *     @Override
 *     protected void beforeWriteGroupRows(Object groupKey, List<RowDTO> groupRows) {
 *         // 组内第 2 行起置空重复字段
 *     }
 *
 *     @Override
 *     protected PagingVO<RowDTO> getPageData(PagingDTO<ParamDTO> dto) {
 *         return feign.exportData(dto); // 须保证按 sheetGroupKey 连续排序
 *     }
 * }
 * }</pre>
 *
 * @param <T> 导出行类型
 * @param <P> 查询参数类型
 * @see AbstractSingleSheetGroupPageFileEventHandler
 * @see AbstractPageFileEventHandler#sheetGroupKey(Object)
 * @see AbstractPageFileEventHandler#beforeWriteGroupRows(Object, java.util.List)
 */
public abstract class AbstractMultiSheetGroupPageFileEventHandler<T, P> extends AbstractPageFileEventHandler<T, P> {

    /**
     * 多 sheet 分组模式：按 {@link #maxDataRowsPerSheet()} 分页，不强制单 sheet。
     */
    @Override
    protected boolean preferSingleDataSheetForGroupKeeping() {
        return false;
    }

    /**
     * 启用同组不拆 sheet；临界点整组回退到下一 sheet。
     */
    @Override
    protected boolean keepSheetGroupTogether() {
        return true;
    }

    /**
     * 分组临界点回退时额外预留的 sheet 数。默认 1；未使用的预留 sheet 保留为空 tab，不做写后删除。
     * 超大分组或预估 sheet 数接近 {@link #maxTemplateDataSheets()} 时可重写增大。
     */
    @Override
    protected int sheetGroupExtraSheetCount() {
        return 1;
    }
}
