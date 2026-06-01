package com.erp.server.file.core;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ExportPaginationMode;
import com.common.business.vo.KeysetPagingVO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.databind.JavaType;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public abstract class AbstractPageFileEventHandler<T, P> extends AbstractFileEventHandler<T> {

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
            throw new IllegalStateException(getClass().getName()
                    + " 无法推断分页参数类型 P，请确保直接继承 AbstractPageFileEventHandler<T,P> 并指定具体 P，或重写 resolveExportParams");
        }
        JavaType javaType = getObjectMapper().getTypeFactory().constructType(paramType);
        return (P) readValue(fileTask.getMetaInfo(), javaType);
    }

    /**
     * 从当前 Handler 类解析 {@link AbstractPageFileEventHandler} 的第二个类型参数 P。
     */
    static Type resolvePagingParamType(Class<?> handlerClass) {
        ResolvableType rt = ResolvableType.forClass(handlerClass);
        while (rt != ResolvableType.NONE) {
            if (rt.getRawClass() != null && rt.getRawClass() == AbstractPageFileEventHandler.class) {
                ResolvableType param = rt.getGeneric(1);
                if (param != ResolvableType.NONE && null != param.getType()) {
                    return param.getType();
                }
                return null;
            }
            rt = rt.getSuperType();
        }
        return null;
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
        Path tempPath = null;
        try {
            P params = resolveExportParams(fileTask);
            tempPath = ExportTempFilesHandler.createTempPath(FileRegistry.getStorageTmpdir(), ".xlsx", fileTask.getUniqueWithFileName());
            int total = writePagedExcel(tempPath.toFile(), params);
            fileTask.setCount(total);
            String displayName = buildDownloadFileName(fileTask);
            String url = FastDFSClientUtil.uploadFile(tempPath.toFile(), displayName, null);
            fileTask.setFileUrl(url);
        } catch (IOException e) {
            log.error("导出上传失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            ExportTempFilesHandler.deleteQuietly(tempPath);
        }
    }


    protected Integer writePagedExcel(File outFile, P params) throws IOException {
        switch (exportPaginationMode()) {
            case KEYSET_BY_SORT_ID:
                return writeKeysetBatches(outFile, params);
            case OFFSET:
                return writeOffsetBatches(outFile, params);
            default:
                throw new BusinessException(exportPaginationMode().name());
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
        throw new UnsupportedOperationException("启用键集导出时请重写 fetchKeyset(Object,Long,int)");
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
        return Math.max(1, FileRegistry.getSheetMaxRows() - reservedTemplateHeaderRows());
    }

    /**
     * 列表数据区最多占用的物理 sheet 数（含 sheet0）。超出则抛 {@link BusinessException}，避免无限克隆。
     */
    protected int maxTemplateDataSheets() {
        return FileRegistry.getMaxSheetNum();
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
            throw new BusinessException("导出约需 " + sheets + " 张数据表，超过系统上限 " + max
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

    private byte[] readClasspathTemplateBytes(String excelPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(excelPath);
        try (InputStream in = resource.getInputStream()) {
            return IOUtils.toByteArray(in);
        }
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
     * 模板除数据源 sheet 外的其余 sheet 必须为空（仅作占位），否则抛出 {@link BusinessException}；展开时会先移除这些空占位 sheet，
     * 再由数据源 sheet 克隆补齐，保证数据 sheet 物理下标连续（0、1、2…）且不残留中间空 sheet。返回真实物理 sheet 下标映射。
     */
    private ExpandedTemplate expandTemplateWithDataSheetCopies(byte[] templateBytes, int dataSheetCount) throws IOException {
        int source = templateSourceSheetIndex();
        try (ByteArrayInputStream bin = new ByteArrayInputStream(templateBytes);
                XSSFWorkbook wb = new XSSFWorkbook(bin)) {
            int originalSheetCount = wb.getNumberOfSheets();
            // 模板仅一个 sheet 且只需一个数据 sheet：无需展开
            if (originalSheetCount == 1 && dataSheetCount <= 1) {
                return new ExpandedTemplate(templateBytes, Collections.singletonList(source));
            }
            String sourceSheetName = wb.getSheetName(source);
            // 除数据源（第一个）sheet 外，模板其余 sheet 必须为空（仅作可被替换的占位 sheet）；非空则报错
            for (int i = 0; i < originalSheetCount; i++) {
                if (i == source) {
                    continue;
                }
                if (wb.getSheetAt(i).getPhysicalNumberOfRows() > 0) {
                    throw new BusinessException("导出模板除第一个数据 sheet 外存在非空 sheet（sheet=" + wb.getSheetName(i)
                            + "），无法用于分页多 sheet 导出，请将其清空或从模板中移除。");
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

    private void logEasyExcelFillContext(String exportPhase, Throwable ex, String pagingState, int sheetNo,
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
                exportPhase, getClass().getName(), getExcelPath(), pagingState, sheetNo, rowsInSheet, batchSize,
                nullInOriginal, firstRowClass, ex);
    }

    private static final class OffsetSheetCursor {
        int sheetNo;
        long rowsInSheet;
        WriteSheet writeSheet;
        List<Integer> dataSheetIndexes;
    }

    private WriteSheet buildCurrentDataSheet(OffsetSheetCursor c) {
        if (c.sheetNo >= c.dataSheetIndexes.size()) {
            throw new BusinessException("导出数据超过当前模板可承载的 sheet 数，请缩小筛选范围导出。");
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
    private void fillBatchAcrossDataSheets(String exportPhase, ExcelWriter excelWriter, FillConfig fillConfig,
            List<T> batch, List<T> rawPageForLog, OffsetSheetCursor c, String pagingState, int dataTotalCount) {
        int maxPerConfigured = maxDataRowsPerSheet();
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
                throw new BusinessException(
                        "导出数据超过 Excel 单 sheet 最大行数（约 104 万行），请缩小筛选范围或拆分导出。");
            }
            int take = Math.min(Math.min(roomConfigured, hardRoom), batch.size() - idx);
            List<T> slice = batch.subList(idx, idx + take);
            List<T> fillList = (take == batch.size() && idx == 0) ? batch : new ArrayList<>(slice);
            try {
                fillOnSheet(excelWriter, c.writeSheet, fillList, fillConfig);
            } catch (Throwable fillEx) {
                logEasyExcelFillContext(exportPhase, fillEx, pagingState + ",totalCount=" + dataTotalCount, c.sheetNo,
                        c.rowsInSheet, fillList, rawPageForLog);
                throw fillEx;
            }
            if (fillList != batch) {
                fillList.clear();
            }
            c.rowsInSheet += take;
            idx += take;
        }
    }

    private int writeKeysetBatches(File outFile, P params) throws IOException {
        ExcelPrintUtils excelPrintUtils = new ExcelPrintUtils();
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
        Long lastId = null;
        int preparedSheets = keysetPreparedSheetCount();
        byte[] rawTemplate = readClasspathTemplateBytes(getExcelPath());
        // 预先克隆好多张数据 sheet，避免在写入过程中再 clone 导致的性能问题（尤其是当模板复杂时）。若数据量超出预估则直接报错，避免无限克隆。
        ExpandedTemplate expandedTemplate = expandTemplateWithDataSheetCopies(rawTemplate, preparedSheets);
        rawTemplate = null;

        int total = 0;
        OffsetSheetCursor cursor = new OffsetSheetCursor();
        cursor.sheetNo = 0;
        cursor.rowsInSheet = 0;
        cursor.dataSheetIndexes = expandedTemplate.dataSheetIndexes;
        WriteHandler[] handlers = getWriteHandler().toArray(new WriteHandler[0]);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = excelPrintUtils.openTemplateListWriter(fos, expandedTemplate.templateBytes, handlers);
            try {
                cursor.writeSheet = buildCurrentDataSheet(cursor);
                while (true) {
                    KeysetPagingVO<T> vo = fetchKeyset(params, lastId, getPageSize());
                    List<T> rawList = vo.getList();
                    if (CollectionUtils.isEmpty(rawList)) {
                        break;
                    }
                    List<T> batch = withoutNullListElements(rawList);
                    if (batch.isEmpty()) {
                        throw new BusinessException("导出数据存在空行，请检查查询结果");
                    }
                    long cap = (long) preparedSheets * maxDataRowsPerSheet();
                    if (total + batch.size() > cap) {
                        throw new BusinessException("键集导出数据量超过当前模板可承载的上限（约 " + cap
                                + " 行），请缩小筛选范围或改用 OFFSET 导出。");
                    }
                    fillBatchAcrossDataSheets("KEYSET", excelWriter, fillConfig, batch, rawList, cursor,
                            "lastIdExclusive=" + lastId + ",hasNext=" + vo.isHasNext(), 0);
                    total += batch.size();
                    Long next = vo.getNextCursorId();
                    if (next == null) {
                        next = maxSortId(batch);
                    }
                    if (next == null) {
                        throw new BusinessException("键集导出无法推导下一游标，请在 KeysetPagingVO 中设置 nextCursorId 或重写 extractSortId");
                    }
                    lastId = next;
                    clearBatchIfDetachedCopy(batch, rawList);
                    if (!vo.isHasNext()) {
                        break;
                    }
                }
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

    private int writeOffsetBatches(File outFile, P params) throws IOException {
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(getFirstPage());
        dto.setParams(params);

        PagingVO<T> firstData = getPageData(dto);
        int totalCount = firstData.getTotalCount();
        int dataSheets = computeDataSheetCountForTotalRows(totalCount);
        byte[] rawTemplate = readClasspathTemplateBytes(getExcelPath());
        ExpandedTemplate expandedTemplate = expandTemplateWithDataSheetCopies(rawTemplate, dataSheets);
        rawTemplate = null;

        int totalRows = 0;
        OffsetSheetCursor cursor = new OffsetSheetCursor();
        cursor.sheetNo = 0;
        cursor.rowsInSheet = 0;
        cursor.dataSheetIndexes = expandedTemplate.dataSheetIndexes;
        WriteHandler[] handlers = getWriteHandler().toArray(new WriteHandler[0]);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expandedTemplate.templateBytes, handlers);
            try {
                cursor.writeSheet = buildCurrentDataSheet(cursor);
                PagingVO<T> pageData = firstData;
                while (true) {
                    int pageListSize = CollectionUtils.isEmpty(pageData.getList()) ? 0 : pageData.getList().size();
                    long cap = (long) dataSheets * maxDataRowsPerSheet();
                    if (totalRows + pageListSize > cap) {
                        throw new BusinessException("导出数据量超过当前模板可承载的上限（约 " + cap
                                + " 行），请缩小筛选范围导出。");
                    }
                    if (!CollectionUtils.isEmpty(pageData.getList())) {
                        List<T> rawPage = pageData.getList();
                        List<T> batch = withoutNullListElements(rawPage);
                        if (!batch.isEmpty()) {
                            fillBatchAcrossDataSheets("OFFSET", excelWriter, fillConfig, batch, rawPage, cursor,
                                    "currPage=" + dto.getCurrPage(), pageData.getTotalCount());
                            totalRows += batch.size();
                            clearBatchIfDetachedCopy(batch, rawPage);
                        }
                    }
                    if (1 == getFirstPage()) {
                        if (totalCount <= dto.getCurrPage() * getPageSize()) {
                            break;
                        }
                    } else {
                        if (totalCount <= (dto.getCurrPage() + 1) * getPageSize()) {
                            break;
                        }
                    }
                    dto.setCurrPage(dto.getCurrPage() + 1);
                    pageData = getPageData(dto);
                }
            } finally {
                excelWriter.finish();
            }
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
            PagingVO<T> data = getPageData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                dataList.addAll((Collection<? extends T>) data.getList());
            }
            if (totalCount == 0) {
                totalCount = data.getTotalCount();
            }
            if (1 == getFirstPage()) {
                if (totalCount <= dto.getCurrPage() * getPageSize()) {
                    hasNext = false;
                }
            } else {
                if (totalCount <= (dto.getCurrPage() + 1) * getPageSize()) {
                    hasNext = false;
                }
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        return dataList;
    }

    protected int getPageSize() {
        return 5000;
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
}
