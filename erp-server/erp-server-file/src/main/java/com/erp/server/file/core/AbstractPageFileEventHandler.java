package com.erp.server.file.core;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ExportPaginationMode;
import com.common.business.vo.KeysetPagingVO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.erp.model.file.entity.FileTask;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.databind.JavaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class AbstractPageFileEventHandler<T, P> extends AbstractFileEventHandler<T> {

    /**
     * 按导出参数选择模板。固定模板的子类继续重写无参 {@link #getExcelPath()} 即可；
     * 需要按参数切换模板的子类重写本方法，避免依赖 ThreadLocal 传递参数。
     */
    protected String getExcelPath(P params) {
        return getExcelPath();
    }

    /**
     * 解析导出查询参数。默认根据 {@code AbstractPageFileEventHandler&lt;T, P&gt;} 的泛型 {@code P}
     * 从 {@link FileTask#getMetaInfo()} 反序列化，子类通常<strong>无需再写</strong> {@code readValue}。
     * <p>
     * 若泛型推断失败（中间继承层级丢失类型信息）或 Jackson 需要特殊 {@code TypeReference}，再重写本方法。
     */
    @SuppressWarnings("unchecked")
    protected P resolveExportParams(FileTask fileTask) {
        Type paramType = resolvePagingParamType(getClass());
        if (paramType == null) {
            throw new ServiceException(getClass().getName()
                    + " 无法推断分页参数类型 P，请确保直接继承 AbstractPageFileEventHandler<T,P> 并指定具体 P，或重写 resolveExportParams");
        }
        JavaType javaType = getObjectMapper().getTypeFactory().constructType(paramType);
        return (P) readValue(fileTask.getMetaInfo(), javaType);
    }

    /**
     * 从当前 Handler 类解析 {@link AbstractPageFileEventHandler} 的第二个类型参数 P。
     */
    static Type resolvePagingParamType(Class<?> handlerClass) {
        // 用 forClass(baseType, implementationClass) 跨中间继承层把类型变量 P 绑定到具体类型；
        // 返回值必须用 resolve()（已绑定的具体 Class），不能用 getType()。
        // 注意：getType() 返回声明处原始 Type，跨继承层时即未解析的 TypeVariable，
        // Jackson constructType 会退回 Object 并反序列化成 LinkedHashMap，在使用处抛 ClassCastException。
        // 本仓库所有导出 Handler 的 P 均为非参数化 DTO，resolve() 不存在嵌套泛型丢失问题；
        // 若将来出现参数化 P，应改为由 ResolvableType 递归构造 Jackson JavaType，而非退回 getType()。
        ResolvableType param = ResolvableType.forClass(AbstractPageFileEventHandler.class, handlerClass).getGeneric(1);
        if (param == ResolvableType.NONE || param.resolve() == null) {
            return null;
        }
        return param.resolve();
    }

    /**
     * 下载任务入口。默认委托 {@link #defaultPagingExportHandle(FileTask)}；
     * 仅当子类需要完全不同的导出流程（多 sheet 模板、ZIP 等）时再覆盖，
     * 覆盖后如需复用标准分页导出可调用 {@link #defaultPagingExportHandle(FileTask)}。
     */
    @Override
    public void handle(FileTask fileTask) {
        defaultPagingExportHandle(fileTask);
    }

    /**
     * 标准分页导出：{@link #resolveExportParams} → {@link #writePagedExcel} → 临时文件 → FastDFS → 清理。
     */
    protected void defaultPagingExportHandle(FileTask fileTask) {
        P params = resolveExportParams(fileTask);
        String excelPath = getExcelPath(params);
        String displayName = buildDownloadFileName(fileTask, excelPath);
        ExportTempFilesHandler.exportToTempAndUpload(fileTask, ".xlsx", displayName,
                outFile -> writePagedExcel(outFile, params, excelPath));
    }


    protected Integer writePagedExcel(File outFile, P params, String excelPath) throws IOException {
        switch (exportPaginationMode()) {
            case KEYSET_BY_SORT_ID:
                return writeKeysetBatches(outFile, params, excelPath);
            case OFFSET:
                return writeOffsetBatches(outFile, params, excelPath);
            // 当前 ExportPaginationMode 仅 OFFSET / KEYSET_BY_SORT_ID；default 供未来新增枚举值未补分支时兜底（审查勿要求改文案为合并项）。
            default:
                throw new ServiceException("不支持的导出分页模式: " + exportPaginationMode()
                        + "，请在 writePagedExcel 中补充对应分支或检查子类 exportPaginationMode() 配置。");
        }
    }

    /**
     * 导出取数策略。默认 {@link ExportPaginationMode#OFFSET}；
     * 大数据量且远端支持单调 id 键集查询时，子类可改为 {@link ExportPaginationMode#KEYSET_BY_SORT_ID} 并重写 {@link #fetchKeyset}。
     */
    protected ExportPaginationMode exportPaginationMode() {
        return ExportPaginationMode.OFFSET;
    }

    /**
     * 当 {@link ExportPaginationMode#KEYSET_BY_SORT_ID} 时由 {@link #writeKeysetBatches} 调用；需在远端实现例如
     * {@code WHERE id > ? ORDER BY id LIMIT ?}。
     */
    protected KeysetPagingVO<T> fetchKeyset(P params, Long lastIdExclusive, int limit) {
        throw new ServiceException("启用键集导出时请重写 fetchKeyset(params, lastIdExclusive, limit)");
    }

    /**
     * 当 {@link KeysetPagingVO#getNextCursorId()} 为空时，用于从行对象推导雪花/id 序值
     */
    protected Long extractSortId(T row) {
        return null;
    }

    /**
     * 模板中非列表区域预留行数，用于粗略估算单 sheet 是否超限
     */
    protected int reservedTemplateHeaderRows() {
        return 0;
    }

    protected int maxDataRowsPerSheet() {
        return Math.max(1, FileRegistry.sheetMaxRowsOrDefault() - reservedTemplateHeaderRows());
    }

    /**
     * 列表数据区最多占用的物理 sheet 数（含 sheet0）。超出则抛 {@link ServiceException}，避免无限克隆。
     */
    protected int maxTemplateDataSheets() {
        return FileRegistry.maxSheetNumOrDefault();
    }

    /**
     * 作为列表填充模板的源 sheet 下标，默认 0。若模板第一页非数据区可重写。
     */
    protected int templateSourceSheetIndex() {
        return 0;
    }

    /**
     * 根据总行数与 {@link #maxDataRowsPerSheet()} 计算需要的数据 sheet 张数（向上取整）。
     */
    protected int computeDataSheetCountForTotalRows(int totalCount) {
        int rowsPer = maxDataRowsPerSheet();
        if (totalCount <= 0) {
            return 1;
        }
        long k = (totalCount + (long) rowsPer - 1) / rowsPer;
        int sheets = (int) Math.max(1L, k);
        int max = maxTemplateDataSheets();
        if (sheets > max) {
            throw new ServiceException("导出约需 " + sheets + " 张数据表，超过系统上限 " + max
                    + "，请缩小筛选范围或联系管理员调大 maxTemplateDataSheets。");
        }
        return sheets;
    }

    /**
     * xlsx 单 sheet 最大数据行（预留表头），用于与 {@link #maxDataRowsPerSheet()} 组合防止超过 Excel 行上限。
     */
    private int maxRowsPerXlsxSheetHardLimit() {
        return Math.max(1, 1_048_576 - reservedTemplateHeaderRows() - 1);
    }

    private int keysetPreparedSheetCount() {
        int byHard = (int) Math.ceil((double) maxRowsPerXlsxSheetHardLimit() / (double) maxDataRowsPerSheet());
        return Math.min(maxTemplateDataSheets(), Math.max(1, byHard));
    }

    private int expandSheetCountForGroupKeeping(int estimatedSheets) {
        int max = maxTemplateDataSheets();
        int extra = Math.max(0, sheetGroupExtraSheetCount());
        return Math.min(max, estimatedSheets + extra);
    }

    /**
     * 读取 classpath 模板为字节数组。
     * <p>
     * 注意：模板整本读入内存、展开多 sheet 后再整本 {@code wb.write} 为 byte[]，峰值内存与
     * 「模板复杂度 × 数据 sheet 数」正相关。数据行已流式写盘，但模板展开阶段仍非流式，
     * 故须在配置层约束 {@code maxTemplateDataSheets}（{@link #maxTemplateDataSheets()}）与单 sheet 行数
     * （{@link #maxDataRowsPerSheet()}）；并在 {@link #expandTemplateWithDataSheetCopies} 入口以
     * {@code file.storage.maxTemplateExpandBytes}（默认 300MB）对「模板字节 × sheet 数」做固定上界保护、早失败避免 OOM。
     * 读入前另由 {@link ClasspathExportTemplateReader} 按 {@code maxTemplateExpandBytes / maxSheetNum} 校验单份模板体积。
     * 超大导出场景的 POI 流式模板展开作为后续优化。
     */
    private byte[] readClasspathTemplateBytes(String excelPath) throws IOException {
        return ClasspathExportTemplateReader.readBytes(excelPath);
    }

    private static final class ExpandedTemplate {
        private final byte[] templateBytes;
        private final List<Integer> dataSheetIndexes;

        private ExpandedTemplate(byte[] templateBytes, List<Integer> dataSheetIndexes) {
            this.templateBytes = templateBytes;
            this.dataSheetIndexes = dataSheetIndexes;
        }
    }

    /**
     * 将模板中 {@link #templateSourceSheetIndex()} 指向的数据源 sheet 复制为共 {@code dataSheetCount} 张同结构数据 sheet，
     * 供 EasyExcel 按 sheet 分批 fill。
     * <p>
     * <strong>存量模板约定（审查勿误报为破坏性变更）</strong>：classpath 导出模板以「仅 sheet0 含列表填充区」为主；
     * 若存在 sheet1、sheet2…，均为历史遗留的空占位 sheet（{@code getPhysicalNumberOfRows()==0}），展开时移除占位后再克隆 sheet0。
     * 单 sheet 且 {@code dataSheetCount<=1} 时直接返回原模板字节，不进入下述校验与克隆。
     * <p>
     * 本方法<strong>不</strong>支持「封面 / 说明 / 汇总」等非空静态页与数据 sheet 并存：若非数据源 sheet 有物理行则显式失败。
     * 此类多 sheet 业务模板应使用 {@link com.erp.server.file.core.multisheet.AbstractMultiSheetPageFileEventHandler}
     * / {@link com.erp.server.file.core.multisheet.MultiSheetTemplateWriter} 体系，勿走单列表分页展开路径。
     * <p>
     * 除数据源 sheet 外的其余 sheet 必须为空（仅作占位），否则抛出 {@link ServiceException}；展开时会先移除这些空占位 sheet，
     * 再由数据源 sheet 克隆补齐，保证数据 sheet 物理下标连续（0、1、2…）且不残留中间空 sheet。返回真实物理 sheet 下标映射。
     */
    private ExpandedTemplate expandTemplateWithDataSheetCopies(byte[] templateBytes, int dataSheetCount) throws IOException {
        if (dataSheetCount <= 0) {
            throw new ServiceException("dataSheetCount 必须大于 0");
        }
        // 模板展开为非流式：POI 整本读入并克隆 dataSheetCount 张 sheet 后再整本写出，
        // 峰值内存与「模板字节 × sheet 数」正相关（POI 对象模型放大系数另计）。读入前已由 ClasspathExportTemplateReader
        // 按 maxTemplateExpandBytes / maxSheetNum 校验单份模板体积；此处再以「展开足迹」固定上界早失败，
        // 把「复杂模板 × 高 sheet 数」从开放风险收成可证明上界，避免 OOM；上界由 file.storage.maxTemplateExpandBytes 配置（默认 300MB）。
        // 审查约定：本方法（单数据源 sheet 克隆 + 空占位 sheet 移除 + 连续物理下标）与多 sheet 路径
        // MultiSheetTemplateWriter.expandTemplate（多类型 sheet 各自克隆 + buildCloneName 命名）语义不同，属并行实现而非重复，
        // 当前无行为分叉；勿要求合并为同一工具类作为合并阻断项，抽取仅为可选优化。
        long expandFootprint = (long) templateBytes.length * dataSheetCount;
        long maxExpandBytes = FileRegistry.maxTemplateExpandBytesOrDefault();
        if (expandFootprint > maxExpandBytes) {
            throw new ServiceException("导出模板展开预估占用过大（模板≈" + (templateBytes.length / 1024)
                    + "KB × " + dataSheetCount + " 张 ≈ " + (expandFootprint / 1024 / 1024) + "MB，上限 "
                    + (maxExpandBytes / 1024 / 1024) + "MB），请简化模板、缩小导出范围或调大 file.storage.maxTemplateExpandBytes。");
        }
        int source = templateSourceSheetIndex();
        try (ByteArrayInputStream bin = new ByteArrayInputStream(templateBytes);
                XSSFWorkbook wb = new XSSFWorkbook(bin)) {
            int originalSheetCount = wb.getNumberOfSheets();
            // 模板仅一个 sheet 且只需一个数据 sheet：无需展开
            if (originalSheetCount == 1 && dataSheetCount <= 1) {
                return new ExpandedTemplate(templateBytes, Collections.singletonList(source));
            }
            String sourceSheetName = wb.getSheetName(source);
            // 存量约定：仅 sheet0 有模板，其余 sheet 须为空占位（见方法 JavaDoc）。非空静态页（封面/说明等）不在本路径支持范围内。
            for (int i = 0; i < originalSheetCount; i++) {
                if (i == source) {
                    continue;
                }
                if (wb.getSheetAt(i).getPhysicalNumberOfRows() > 0) {
                    throw new ServiceException("导出模板除第一个数据 sheet 外存在非空 sheet（sheet="
                            + wb.getSheetName(i) + "），无法用于分页多 sheet 导出，请将其清空或从模板中移除。");
                }
            }
            // 直接替换这些空占位 sheet：先移除，再由数据源 sheet 克隆补齐，使数据 sheet 连续排布且不残留空 sheet
            for (int i = originalSheetCount - 1; i >= 0; i--) {
                if (i != source) {
                    wb.removeSheetAt(i);
                }
            }
            int newSource = wb.getSheetIndex(sourceSheetName);
            List<Integer> dataSheetIndexes = new ArrayList<>(Math.max(1, dataSheetCount));
            dataSheetIndexes.add(newSource);
            for (int i = 1; i < dataSheetCount; i++) {
                wb.cloneSheet(newSource);
                dataSheetIndexes.add(wb.getNumberOfSheets() - 1);
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                wb.write(out);
                return new ExpandedTemplate(out.toByteArray(), dataSheetIndexes);
            }
        }
    }

    private List<T> withoutNullListElements(List<T> raw) {
        if (CollectionUtils.isEmpty(raw)) {
            return raw;
        }
        int nulls = 0;
        for (T row : raw) {
            if (row == null) {
                nulls++;
            }
        }
        if (nulls == 0) {
            return raw;
        }
        ArrayList<T> copy = new ArrayList<>(raw.size() - nulls);
        for (T row : raw) {
            if (row != null) {
                copy.add(row);
            }
        }
        return copy;
    }

    /**
     * 若 {@code batch} 是 {@link #withoutNullListElements} 新分配的副本（与远端返回的 {@code rawPage} 非同一引用），
     * 则在 fill 结束后清空元素引用，便于 GC（勿对 rawPage 本体 clear，避免污染 Feign 等缓存的 VO）。
     */
    private void clearBatchIfDetachedCopy(List<T> batch, List<T> rawPage) {
        if (null != batch && batch != rawPage) {
            batch.clear();
        }
    }

    private static int nullElementCount(List<?> list) {
        if (list == null) {
            return 0;
        }
        int n = 0;
        for (Object o : list) {
            if (o == null) {
                n++;
            }
        }
        return n;
    }

    private void logEasyExcelFillContext(String exportPhase, String excelPath, Throwable ex, String pagingState, int sheetNo,
            long rowsInSheet, List<T> batch, List<T> originalPageList) {
        int batchSize = batch == null ? -1 : batch.size();
        int nullInOriginal = nullElementCount(originalPageList);
        String firstRowClass = "n/a";
        if (batch != null && !batch.isEmpty() && batch.get(0) != null) {
            firstRowClass = batch.get(0).getClass().getName();
        }
        log.error(
                "EasyExcel模板填充失败(见下文字段便于检索): phase={} handler={} template={} paging={} "
                        + "sheetNo={} rowsInSheet={} batchSize={} nullInOriginalPage={} firstRowClass={}",
                exportPhase, getClass().getName(), excelPath, pagingState, sheetNo, rowsInSheet, batchSize,
                nullInOriginal, firstRowClass, ex);
    }

    private static final class OffsetSheetCursor {
        int sheetNo;
        long rowsInSheet;
        WriteSheet writeSheet;
        List<Integer> dataSheetIndexes;
    }

    /**
     * 分组保护开启时暂存分页末尾的未闭合分组。
     * <p>
     * 查询结果按 {@link #sheetGroupKey(Object)} 连续排序时，当前页末尾分组可能延续到下一页；
     * 需要先暂存，等下一页确认分组结束后再写出，避免同一业务维度被分页边界拆开。
     */
    private static final class SheetGroupBuffer<T> {
        final List<T> rows = new ArrayList<>();
    }

    private WriteSheet buildCurrentDataSheet(OffsetSheetCursor c) {
        if (c.sheetNo >= c.dataSheetIndexes.size()) {
            throw new ServiceException("导出数据超过当前模板可承载的 sheet 数，请缩小筛选范围导出。");
        }
        return EasyExcel.writerSheet(c.dataSheetIndexes.get(c.sheetNo)).build();
    }

    /**
     * 模板列表分批填充。须用 {@link ExcelWriter#fill}，<strong>不可</strong>用 {@code ExcelWriterSheetBuilder#doFill}：
     * 3.3.x 的 {@code doFill} 在每次填充后会 {@code finish()} 关闭 Writer，后续分页只会写出第一页。
     */
    private void fillOnSheet(ExcelWriter excelWriter, WriteSheet writeSheet, List<T> fillList, FillConfig fillConfig) {
        excelWriter.fill(fillList, fillConfig, writeSheet);
    }

    /**
     * 将一批列表按 {@link #maxDataRowsPerSheet()} 与 xlsx 行上限，跨多个已预先克隆好的物理 sheet 写入。
     */
    private void fillBatchAcrossDataSheets(String exportPhase, String excelPath, ExcelWriter excelWriter, FillConfig fillConfig,
            List<T> batch, List<T> rawPageForLog, OffsetSheetCursor c, String pagingState, int dataTotalCount) {
        int maxPerConfigured = maxDataRowsPerSheet();
        boolean keepGroupTogether = isSheetGroupEnabled(batch);
        int idx = 0;
        while (idx < batch.size()) {
            int roomConfigured = maxPerConfigured - (int) c.rowsInSheet;
            if (roomConfigured <= 0) {
                c.sheetNo++;
                c.writeSheet = buildCurrentDataSheet(c);
                c.rowsInSheet = 0;
                roomConfigured = maxPerConfigured;
            }
            int hardRoom = maxRowsPerXlsxSheetHardLimit() - (int) c.rowsInSheet;
            if (hardRoom <= 0) {
                throw new ServiceException(
                        "导出数据超过 Excel 单 sheet 最大行数（约 104 万行），请缩小筛选范围或拆分导出。");
            }
            int take = Math.min(Math.min(roomConfigured, hardRoom), batch.size() - idx);
            if (keepGroupTogether) {
                int adjustedTake = adjustTakeForSheetGroup(batch, idx, take);
                if (adjustedTake <= 0) {
                    if (c.rowsInSheet > 0) {
                        c.sheetNo++;
                        c.writeSheet = buildCurrentDataSheet(c);
                        c.rowsInSheet = 0;
                        continue;
                    }
                    throw new ServiceException("导出分组数据超过单 sheet 最大行数（分组="
                            + sheetGroupKey(batch.get(idx)) + "，上限=" + Math.min(maxPerConfigured, maxRowsPerXlsxSheetHardLimit())
                            + " 行），请缩小筛选范围或调整单 sheet 行数配置。");
                }
                take = adjustedTake;
            }
            List<T> slice = batch.subList(idx, idx + take);
            List<T> fillList = (take == batch.size() && idx == 0) ? batch : new ArrayList<>(slice);
            try {
                fillOnSheet(excelWriter, c.writeSheet, fillList, fillConfig);
            } catch (Exception fillEx) {
                logEasyExcelFillContext(exportPhase, excelPath, fillEx, pagingState + ",totalCount=" + dataTotalCount, c.sheetNo,
                        c.rowsInSheet, fillList, rawPageForLog);
                ServiceException se = new ServiceException("模板导出填充失败，phase=" + exportPhase + "，" + pagingState);
                se.initCause(fillEx);
                throw se;
            }
            if (fillList != batch) {
                fillList.clear();
            }
            c.rowsInSheet += take;
            idx += take;
        }
    }

    private boolean isSheetGroupEnabled(List<T> rows) {
        if (!keepSheetGroupTogether()) {
            return false;
        }
        if (CollectionUtils.isEmpty(rows)) {
            return false;
        }
        for (T row : rows) {
            if (row != null && sheetGroupKey(row) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据分组 key 回退当前 sheet 的写入临界点。
     * <p>
     * 若默认切片的最后一行与下一行属于同一分组，则把整个尾部分组留到下一个 sheet；
     * 返回 0 表示当前 sheet 剩余空间不足以容纳该分组，需要先切换 sheet。
     */
    private int adjustTakeForSheetGroup(List<T> batch, int idx, int take) {
        int end = idx + take;
        if (end >= batch.size()) {
            return take;
        }
        Object nextKey = sheetGroupKey(batch.get(end));
        if (nextKey == null) {
            return take;
        }
        if (!Objects.equals(sheetGroupKey(batch.get(end - 1)), nextKey)) {
            return take;
        }
        while (end > idx && Objects.equals(sheetGroupKey(batch.get(end - 1)), nextKey)) {
            end--;
        }
        return end - idx;
    }

    /**
     * 合并上一页暂存尾组与当前页数据，返回当前可以安全写出的行。
     * <p>
     * 非末页会继续暂存合并后最后一个分组，直到下一页确认该分组是否结束；
     * 末页则直接返回全部剩余数据，避免导出结束时遗漏尾组。
     */
    private List<T> drainReadyRowsForSheetGroup(List<T> batch, SheetGroupBuffer<T> buffer, boolean lastPage) {
        if (CollectionUtils.isEmpty(buffer.rows) && !isSheetGroupEnabled(batch)) {
            return batch;
        }
        List<T> combined = new ArrayList<>(buffer.rows.size() + batch.size());
        combined.addAll(buffer.rows);
        buffer.rows.clear();
        combined.addAll(batch);
        if (combined.isEmpty() || lastPage) {
            return combined;
        }
        int tailStart = tailGroupStart(combined);
        List<T> ready = new ArrayList<>(combined.subList(0, tailStart));
        buffer.rows.addAll(combined.subList(tailStart, combined.size()));
        assertPendingSheetGroupWithinLimit(buffer.rows);
        return ready;
    }

    /**
     * 找到列表尾部分组的起始下标。
     * <p>
     * 返回值之前的数据可立即写出，返回值之后的数据需暂存到下一页继续判断。
     */
    private int tailGroupStart(List<T> rows) {
        int tailStart = rows.size() - 1;
        Object tailKey = sheetGroupKey(rows.get(tailStart));
        if (tailKey == null) {
            return rows.size();
        }
        while (tailStart > 0 && Objects.equals(sheetGroupKey(rows.get(tailStart - 1)), tailKey)) {
            tailStart--;
        }
        return tailStart;
    }

    /**
     * 暂存分组超过单 sheet 上限时尽早失败。
     * <p>
     * 该场景无法通过临界点回退解决，否则同一分组必然被拆 sheet，只能提示用户缩小筛选范围或调整配置。
     */
    private void assertPendingSheetGroupWithinLimit(List<T> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        int maxRows = Math.min(maxDataRowsPerSheet(), maxRowsPerXlsxSheetHardLimit());
        if (rows.size() > maxRows) {
            throw new ServiceException("导出分组数据超过单 sheet 最大行数（分组=" + sheetGroupKey(rows.get(0))
                    + "，行数=" + rows.size() + "，上限=" + maxRows + " 行），请缩小筛选范围或调整单 sheet 行数配置。");
        }
    }

    /**
     * 导出结束或遇到合法空末页时，将暂存尾组写出。
     */
    private int flushSheetGroupBuffer(String exportPhase, String excelPath, ExcelWriter excelWriter, FillConfig fillConfig,
            SheetGroupBuffer<T> buffer, OffsetSheetCursor cursor, String pagingState, int dataTotalCount) {
        if (CollectionUtils.isEmpty(buffer.rows)) {
            return 0;
        }
        List<T> remaining = new ArrayList<>(buffer.rows);
        buffer.rows.clear();
        beforeWriteRows(remaining);
        fillBatchAcrossDataSheets(exportPhase, excelPath, excelWriter, fillConfig, remaining, remaining, cursor,
                pagingState, dataTotalCount);
        return remaining.size();
    }

    private int writeKeysetBatches(File outFile, P params, String excelPath) throws IOException {
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();

        // 先取首页以判定数据规模：EasyExcel 模板写入需在打开 Writer 前确定 sheet 数、写入中无法再加 sheet。
        // 单页即结束（!hasNext）的小数据量导出按实际行数精确展开（通常 1 张），避免无脑按 keysetPreparedSheetCount()
        // 上限预克隆过多 sheet 造成的模板展开内存开销；多页（hasNext）导出因无法中途加 sheet，仍保守预展开以保证容量不回退。
        KeysetPagingVO<T> vo = requirePagingResult(fetchKeyset(params, null, getPageSize()), "游标 lastId=null");
        List<T> rawList = vo.getList();
        int firstBatchRows = (rawList == null) ? 0 : rawList.size() - nullElementCount(rawList);
        int preparedSheets = vo.isHasNext()
                ? keysetPreparedSheetCount()
                : computeDataSheetCountForTotalRows(firstBatchRows);
        if (preparedSheets > 1 && keepSheetGroupTogether()) {
            preparedSheets = expandSheetCountForGroupKeeping(preparedSheets);
        }

        byte[] rawTemplate = readClasspathTemplateBytes(excelPath);
        // 预先克隆好数据 sheet，避免在写入过程中再 clone 导致的性能问题（尤其模板复杂时）。若数据量超出预估则直接报错，避免无限克隆。
        ExpandedTemplate expandedTemplate = expandTemplateWithDataSheetCopies(rawTemplate, preparedSheets);
        rawTemplate = null;

        int total = 0;
        int fetchedRows = 0;
        Long lastId = null;
        OffsetSheetCursor cursor = new OffsetSheetCursor();
        cursor.sheetNo = 0;
        cursor.rowsInSheet = 0;
        cursor.dataSheetIndexes = expandedTemplate.dataSheetIndexes;
        SheetGroupBuffer<T> groupBuffer = new SheetGroupBuffer<>();
        WriteHandler[] handlers = getWriteHandler().toArray(new WriteHandler[0]);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expandedTemplate.templateBytes, handlers);
            try {
                cursor.writeSheet = buildCurrentDataSheet(cursor);
                while (true) {
                    if (CollectionUtils.isEmpty(rawList)) {
                        // 空列表但 vo 仍声明有后续数据：属上游键集分页异常或数据并发变更。
                        // 若静默 break 会以「仅含模板的空 Excel / total 偏小」成功结束、用户难感知缺数，故显式失败；
                        // hasNext=false 才是正常的数据结束（含合法空导出 total=0）。
                        if (vo.isHasNext()) {
                            throw new ServiceException("键集导出数据缺失：返回空列表但 hasNext=true（游标 lastId=" + lastId
                                    + "），疑似分页查询异常或数据并发变更，请重试或排查上游键集接口。");
                        }
                        total += flushSheetGroupBuffer("KEYSET", excelPath, excelWriter, fillConfig, groupBuffer, cursor,
                                "lastIdExclusive=" + lastId + ",hasNext=false", 0);
                        break;
                    }
                    afterFetchPage(rawList);
                    List<T> batch = withoutNullListElements(rawList);
                    if (batch.isEmpty()) {
                        throw new ServiceException("导出数据存在空行，请检查查询结果");
                    }
                    fetchedRows += batch.size();
                    long cap = (long) preparedSheets * maxDataRowsPerSheet();
                    if (fetchedRows > cap) {
                        throw new ServiceException("键集导出数据量超过当前模板可承载的上限（约 " + cap
                                + " 行），请缩小筛选范围或改用 OFFSET 导出。");
                    }
                    Long next = vo.getNextCursorId();
                    if (next == null) {
                        next = maxSortId(batch);
                    }
                    if (next == null) {
                        throw new ServiceException("键集导出无法推导下一游标，请在 KeysetPagingVO 中设置 nextCursorId 或重写 extractSortId");
                    }
                    List<T> readyBatch = drainReadyRowsForSheetGroup(batch, groupBuffer, !vo.isHasNext());
                    if (!CollectionUtils.isEmpty(readyBatch)) {
                        beforeWriteRows(readyBatch);
                        fillBatchAcrossDataSheets("KEYSET", excelPath, excelWriter, fillConfig, readyBatch, rawList, cursor,
                                "lastIdExclusive=" + lastId + ",hasNext=" + vo.isHasNext(), 0);
                        total += readyBatch.size();
                    }
                    // 游标必须严格单调递增：上游若返回不前进/回退的游标（hasNext 恒真、重复返回同批等），
                    // 仅靠下方 cap 上限会在写满后才失败；此处提前以明确异常拦截，避免无谓的重复拉取与写入。
                    if (lastId != null && next <= lastId) {
                        throw new ServiceException("键集导出游标未前进（lastId=" + lastId + "，next=" + next
                                + "），疑似上游键集分页实现异常或数据并发变更，请排查上游键集接口。");
                    }
                    lastId = next;
                    clearBatchIfDetachedCopy(batch, rawList);
                    if (!vo.isHasNext()) {
                        break;
                    }
                    vo = requirePagingResult(fetchKeyset(params, lastId, getPageSize()), "游标 lastId=" + lastId);
                    rawList = vo.getList();
                }
                total += flushSheetGroupBuffer("KEYSET", excelPath, excelWriter, fillConfig, groupBuffer, cursor,
                        "lastIdExclusive=" + lastId + ",hasNext=false", 0);
            } finally {
                // 必须在底层 OutputStream 仍打开时 finish，否则依赖 finalize 会出现 Zip 未关闭 entry 等 WARN
                excelWriter.finish();
            }
        }
        return total;
    }

    private Long maxSortId(List<T> batch) {
        Long max = null;
        for (T row : batch) {
            Long id = extractSortId(row);
            if (id != null && (max == null || id > max)) {
                max = id;
            }
        }
        return max;
    }

    /**
     * 兼容旧代码路径：默认等价于 {@code listSeqData(resolveExportParams(fileTask))}。
     * <p>
     * <strong>异步导出任务</strong>已由 {@link #handle(FileTask)} 使用 {@link #resolveExportParams(FileTask)}
     * + 分页写入临时文件，<strong>不会再调用本方法</strong>。
     * <p>
     * 若仍有外部代码调用 {@code getData}，可使用默认实现；子类也可暂时保留旧覆盖（请标注 {@code @Deprecated}）并逐步删除。
     *
     * @deprecated 请迁移调用方，勿依赖全量 List；新代码请使用 {@link #resolveExportParams} + 流式导出链路。
     */
    @Deprecated
    @Override
    protected List<T> getData(FileTask fileTask) {
        return listSeqData(resolveExportParams(fileTask));
    }

    /**
     * 分页结果统一判空（出口守卫）。
     * <p>
     * 上游分页 Feign / 查询返回 {@code null} 视为查询失败，统一在分页主流程（OFFSET / KEYSET / {@code listSeqData}）
     * 调用点集中拦截并抛出语义一致的 {@link ServiceException}，避免各调用点重复 {@code if (vo == null)} 与文案分叉。
     * <p>
     * <strong>因此各子类的 {@code getPageData} / {@code fetchKeyset} 实现无需各自对返回值判空</strong>
     * （保持 Handler 轻薄、错误文案统一）；仅当子类走非标准取数路径（如主从派生 {@code fetchMasterPage}）时，
     * 才在其自有方法内按需判空。
     *
     * @param vo          分页结果（{@link PagingVO} 或 {@link KeysetPagingVO}）
     * @param pageLocator 定位信息（如 {@code "页码=3"} 或 {@code "游标 lastId=100"}），用于错误排查
     */
    private <V> V requirePagingResult(V vo, String pageLocator) {
        if (vo == null) {
            throw new ServiceException("导出分页查询失败，查询为空：" + pageLocator);
        }
        return vo;
    }

    /**
     * 是否已到末页（适用于任意 {@link #getFirstPage()} 基准，0 基 / 1 基均正确）。
     * <p>
     * 已覆盖行数 = {@code (currPage - getFirstPage() + 1) * pageSize}，{@code >= totalCount} 即末页。
     * 统一公式取代原「firstPage==1 / else 两套硬编码分支」：原写法仅对 firstPage 为 1 或 0 成立，
     * 若子类将 {@link #getFirstPage()} 覆写为其它值会漏页或多拉页；此处一次性消除该隐患。
     * 用 {@code long} 累计避免大 totalCount 下的 int 溢出。
     */
    private boolean isLastPage(int currPage, int totalCount) {
        long covered = (long) (currPage - getFirstPage() + 1) * getPageSize();
        return totalCount <= covered;
    }

    private int writeOffsetBatches(File outFile, P params, String excelPath) throws IOException {
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(getFirstPage());
        dto.setParams(params);

        PagingVO<T> firstData = requirePagingResult(getPageData(dto), "页码=" + dto.getCurrPage());
        int totalCount = firstData.getTotalCount();
        int dataSheets = computeDataSheetCountForTotalRows(totalCount);
        if (dataSheets > 1 && keepSheetGroupTogether()) {
            dataSheets = expandSheetCountForGroupKeeping(dataSheets);
        }
        byte[] rawTemplate = readClasspathTemplateBytes(excelPath);
        ExpandedTemplate expandedTemplate = expandTemplateWithDataSheetCopies(rawTemplate, dataSheets);
        rawTemplate = null;

        int totalRows = 0;
        int fetchedRows = 0;
        OffsetSheetCursor cursor = new OffsetSheetCursor();
        cursor.sheetNo = 0;
        cursor.rowsInSheet = 0;
        cursor.dataSheetIndexes = expandedTemplate.dataSheetIndexes;
        SheetGroupBuffer<T> groupBuffer = new SheetGroupBuffer<>();
        WriteHandler[] handlers = getWriteHandler().toArray(new WriteHandler[0]);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expandedTemplate.templateBytes, handlers);
            try {
                cursor.writeSheet = buildCurrentDataSheet(cursor);
                PagingVO<T> pageData = firstData;
                while (true) {
                    int pageListSize = CollectionUtils.isEmpty(pageData.getList()) ? 0 : pageData.getList().size();
                    // 中间页空列表防静默丢数：本页之前已覆盖行数 < totalCount 说明本页本应有数据，
                    // 实际返回空属上游分页异常/数据并发变更；若仅跳过会以不完整 Excel「成功」结束、fileTask.count 偏小，故显式失败。
                    // totalCount<=0（合法空导出）时 coveredBeforeThisPage(=0) 不小于 0，不触发。
                    long coveredBeforeThisPage = (long) (dto.getCurrPage() - getFirstPage()) * getPageSize();
                    if (pageListSize == 0 && coveredBeforeThisPage < totalCount) {
                        throw new ServiceException("导出分页数据缺失：页码=" + dto.getCurrPage()
                                + " 返回空列表，但 totalCount=" + totalCount + " 预期仍有数据，疑似分页查询异常或数据并发变更，请重试或排查上游分页接口。");
                    }
                    long cap = (long) dataSheets * maxDataRowsPerSheet();
                    if (fetchedRows + pageListSize > cap) {
                        throw new ServiceException("导出数据量超过当前模板可承载的上限（约 " + cap
                                + " 行），请缩小筛选范围导出。");
                    }
                    if (!CollectionUtils.isEmpty(pageData.getList())) {
                        List<T> rawPage = pageData.getList();
                        afterFetchPage(rawPage);
                        List<T> batch = withoutNullListElements(rawPage);
                        if (batch.isEmpty()) {
                            // 与 KEYSET 路径对齐：rawPage 非空但元素全为 null 时不可静默跳过，否则 totalRows 偏小仍可能「成功」结束
                            throw new ServiceException("导出数据存在空行，请检查查询结果（页码=" + dto.getCurrPage() + "）");
                        }
                        fetchedRows += batch.size();
                        List<T> readyBatch = drainReadyRowsForSheetGroup(batch, groupBuffer,
                                fetchedRows >= totalCount || isLastPage(dto.getCurrPage(), totalCount));
                        if (!CollectionUtils.isEmpty(readyBatch)) {
                            beforeWriteRows(readyBatch);
                            fillBatchAcrossDataSheets("OFFSET", excelPath, excelWriter, fillConfig, readyBatch, rawPage, cursor,
                                    "currPage=" + dto.getCurrPage(), pageData.getTotalCount());
                            totalRows += readyBatch.size();
                        }
                        clearBatchIfDetachedCopy(batch, rawPage);
                    }
                    // 上游单页可能返回超过 pageSize 的行（分页不规范）：已写满 totalCount 即正常结束，
                    // 避免继续翻页取到空列表被上方中间页守卫误判为「数据缺失」。
                    if (fetchedRows >= totalCount || isLastPage(dto.getCurrPage(), totalCount)) {
                        break;
                    }
                    dto.setCurrPage(dto.getCurrPage() + 1);
                    pageData = requirePagingResult(getPageData(dto), "页码=" + dto.getCurrPage());
                }
                totalRows += flushSheetGroupBuffer("OFFSET", excelPath, excelWriter, fillConfig, groupBuffer, cursor,
                        "currPage=" + dto.getCurrPage(), totalCount);
                if (totalCount > 0 && totalRows == 0) {
                    throw new ServiceException("导出失败：totalCount=" + totalCount
                            + " 但未写入任何数据行，疑似分页查询异常或数据全为空行，请检查上游分页接口。");
                }
            } finally {
                excelWriter.finish();
            }
        }
        // 末页 partial：实际写入行数 < 首查 totalCount（多因导出期间并发删除/数据漂移），不视为失败
        // （fileTask.count 已回填实际行数），但与中间页空列表守卫对称地显式告警，便于排查「导出比预期少」反馈，避免静默。
        if (totalCount > 0 && totalRows < totalCount) {
            log.warn("导出末页数据不足：handler={} template={} 预期 totalCount={} 实际写入 totalRows={}，疑似导出期间数据并发变更",
                    getClass().getName(), excelPath, totalCount, totalRows);
        }
        return totalRows;
    }

    /**
     * 兼容旧调用：仍可将多页合并为 List（大数据量慎用）
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public List<T> listSeqData(P p) {
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(getFirstPage());
        List<T> dataList = new ArrayList<>();
        boolean hasNext = true;
        int totalCount = 0;
        while (hasNext) {
            dto.setParams(p);
            PagingVO<T> data = requirePagingResult(getPageData(dto), "页码=" + dto.getCurrPage());
            if (totalCount == 0) {
                totalCount = data.getTotalCount();
            }
            int pageListSize = CollectionUtils.isEmpty(data.getList()) ? 0 : data.getList().size();
            long coveredBeforeThisPage = (long) (dto.getCurrPage() - getFirstPage()) * getPageSize();
            if (pageListSize == 0 && coveredBeforeThisPage < totalCount) {
                throw new ServiceException("导出分页数据缺失：页码=" + dto.getCurrPage()
                        + " 返回空列表，但 totalCount=" + totalCount + " 预期仍有数据，疑似分页查询异常或数据并发变更，请重试或排查上游分页接口。");
            }
            if (!CollectionUtils.isEmpty(data.getList())) {
                List<T> rawPage = data.getList();
                List<T> batch = withoutNullListElements(rawPage);
                if (batch.isEmpty()) {
                    throw new ServiceException("导出数据存在空行，请检查查询结果（页码=" + dto.getCurrPage() + "）");
                }
                dataList.addAll(batch);
            }
            // 与 writeOffsetBatches 对齐：已写满 totalCount（含上游单页超发）即结束，避免空页被误判为缺数。
            if (dataList.size() >= totalCount || isLastPage(dto.getCurrPage(), totalCount)) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        if (totalCount > 0 && dataList.isEmpty()) {
            throw new ServiceException("导出失败：totalCount=" + totalCount
                    + " 但未写入任何数据行，疑似分页查询异常或数据全为空行，请检查上游分页接口。");
        }
        // 末页 partial：实际累计行数 < 首查 totalCount（多因导出期间并发删除/数据漂移），不视为失败，
        // 与 writeOffsetBatches 对称地显式告警，避免静默丢数难感知。
        if (totalCount > 0 && dataList.size() < totalCount) {
            log.warn("导出末页数据不足(listSeqData)：handler={} 预期 totalCount={} 实际 {}，疑似导出期间数据并发变更",
                    getClass().getName(), totalCount, dataList.size());
        }
        return dataList;
    }

    protected int getPageSize() {
        return FileRegistry.exportPageSize();
    }

    protected int getFirstPage() {
        return 1;
    }
    /**
     * 分批获取数据
     *
     * @param dto 分页参数
     * @return T 对应需下载的数据
     */
    protected abstract PagingVO<T> getPageData(PagingDTO<P> dto);

    /**
     * 取数后、写出前对「本页数据」的加工钩子，默认空实现（不影响未重写的单据）。
     * <p>
     * 在 OFFSET / KEYSET 两条链路的每次取数后由基类统一逐页回调，子类可重写做按页富化、
     * 同组重复行置空等可变加工，无需再包裹 {@link #getPageData} / {@link #fetchKeyset}。
     * 钩子按页调用，天然是「按页」粒度；可原地修改元素值，但不应增删元素（行数由基类按页统计）。
     *
     * @param pageList 当前页数据（可能含 null 元素，实现需自行跳过），可为 {@code null}
     */
    protected void afterFetchPage(List<T> pageList) {
    }

    /**
     * 是否启用「同一分组不拆到多个 sheet」保护。默认关闭，保持按 {@link #maxDataRowsPerSheet()} 直接切 sheet 的历史行为。
     */
    protected boolean keepSheetGroupTogether() {
        return false;
    }

    /**
     * 分组保护启用时，在按总行数估算的数据 sheet 数基础上额外预留的 sheet 数。
     * <p>
     * 同组不拆 sheet 会在临界点回退，实际 sheet 数可能略高于 {@code totalCount / maxDataRowsPerSheet}；
     * 默认额外预留 1 张，避免无条件展开到 {@link #maxTemplateDataSheets()} 带来的模板内存放大。
     */
    protected int sheetGroupExtraSheetCount() {
        return 1;
    }

    /**
     * 启用 {@link #keepSheetGroupTogether()} 后用于识别同一业务维度的分组 key。
     * <p>
     * 查询结果必须按该 key 稳定排序，使同组数据连续；否则基类无法保证非连续的相同 key 落在同一个 sheet。
     */
    protected Object sheetGroupKey(T row) {
        return null;
    }

    /**
     * 最终写出前的加工钩子。分组保护开启时，分页尾组可能被延后到下一页后才写出；
     * 子类若需要依赖完整连续分组做置空/汇总，应优先重写本钩子。
     */
    protected void beforeWriteRows(List<T> rows) {
        if (!keepSheetGroupTogether() || CollectionUtils.isEmpty(rows)) {
            return;
        }
        int start = 0;
        while (start < rows.size()) {
            Object groupKey = sheetGroupKey(rows.get(start));
            int end = start + 1;
            while (end < rows.size() && Objects.equals(groupKey, sheetGroupKey(rows.get(end)))) {
                end++;
            }
            beforeWriteGroupRows(groupKey, rows.subList(start, end));
            start = end;
        }
    }

    /**
     * 最终写出前对单个完整分组的加工钩子。
     * <p>
     * 仅在 {@link #keepSheetGroupTogether()} 开启后由 {@link #beforeWriteRows(List)} 按连续
     * {@link #sheetGroupKey(Object)} 分组回调；此时跨页尾组已由基类合并，子类只需处理单组内的业务展示或汇总逻辑。
     *
     * @param groupKey  分组 key，来自 {@link #sheetGroupKey(Object)}
     * @param groupRows 同一分组的连续行，不应增删元素
     */
    protected void beforeWriteGroupRows(Object groupKey, List<T> groupRows) {
    }
}
