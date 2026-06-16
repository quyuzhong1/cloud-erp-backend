package com.erp.server.file.core.multisheet;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.erp.server.file.handler.FileRegistry;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * EasyExcel 2.2.7 多 sheet 分页模板写引擎。
 */
public class MultiSheetTemplateWriter {

    private final String excelPath;
    private final int maxDataRowsPerSheet;
    private final int maxTemplateDataSheets;
    private final int maxRowsPerXlsxSheetHardLimit;
    private final List<WriteHandler> writeHandlers;

    public MultiSheetTemplateWriter(String excelPath,
                                    int maxDataRowsPerSheet,
                                    int maxTemplateDataSheets,
                                    int maxRowsPerXlsxSheetHardLimit,
                                    List<WriteHandler> writeHandlers) {
        this.excelPath = excelPath;
        this.maxDataRowsPerSheet = maxDataRowsPerSheet;
        this.maxTemplateDataSheets = maxTemplateDataSheets;
        this.maxRowsPerXlsxSheetHardLimit = maxRowsPerXlsxSheetHardLimit;
        this.writeHandlers = writeHandlers == null ? Collections.emptyList() : writeHandlers;
    }

    public <P> int streamIndependent(File outFile,
                                     List<IndependentSheet<P>> sheets,
                                     int pageSize,
                                     int firstPage) throws IOException {
        if (CollectionUtils.isEmpty(sheets)) {
            throw new ServiceException("独立分页导出配置不能为空");
        }
        int typeCount = sheets.size();
        @SuppressWarnings("unchecked")
        PagingVO<?>[] firstPages = new PagingVO<?>[typeCount];
        int[] totalRows = new int[typeCount];
        int[] dataSheetCountPerType = new int[typeCount];
        int[] rowLimitPerType = new int[typeCount];
        @SuppressWarnings("unchecked")
        PagingDTO<P>[] pagingDtos = new PagingDTO[typeCount];
        for (int i = 0; i < typeCount; i++) {
            IndependentSheet<P> sheet = sheets.get(i);
            PagingDTO<P> firstDto = newPagingDTO(sheet.getParams(), pageSize, firstPage);
            PagingVO<?> firstVo = sheet.getPageFetcher().apply(firstDto);
            if (firstVo == null) {
                throw new ServiceException("独立分页导出返回为空，sheetIndex=" + i);
            }
            pagingDtos[i] = firstDto;
            firstPages[i] = firstVo;
            totalRows[i] = Math.max(0, firstVo.getTotalCount());
            dataSheetCountPerType[i] = computeConfiguredSheetCount(totalRows[i]);
            rowLimitPerType[i] = Math.min(maxDataRowsPerSheet, maxRowsPerXlsxSheetHardLimit);
        }

        ExpandedTemplate expanded = expandTemplate(readClasspathTemplateBytes(excelPath), dataSheetCountPerType);
        SheetCursor[] cursors = buildCursors(expanded.sheetIndexesPerType, rowLimitPerType);
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
        WriteHandler[] handlers = writeHandlers.toArray(new WriteHandler[0]);

        int actualMainRows = 0;
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expanded.templateBytes, handlers);
            try {
                for (int i = 0; i < typeCount; i++) {
                    int totalPages = computeTotalPages(totalRows[i], pageSize);
                    PagingVO<?> currentVo = firstPages[i];
                    int writtenForType = 0;
                    for (int pageOffset = 0; pageOffset < totalPages; pageOffset++) {
                        int currPage = firstPage + pageOffset;
                        // 当前页（含首页）空列表防静默丢数：本页之前已覆盖行数 < totalCount 说明本页本应有数据，
                        // 实际返回空属上游分页异常/数据并发变更；与 OFFSET 路径（AbstractPageFileEventHandler.writeOffsetBatches）守卫对齐。
                        long coveredBefore = (long) pageOffset * pageSize;
                        if (CollectionUtils.isEmpty(currentVo.getList()) && coveredBefore < totalRows[i]) {
                            throw new ServiceException("独立分页导出数据缺失：sheetIndex=" + i + "，页码=" + currPage
                                    + " 返回空列表，但 totalCount=" + totalRows[i] + " 预期仍有数据，疑似分页查询异常或数据并发变更，请重试或联系开发排查上游分页接口。");
                        }
                        List<?> batch = withoutNullListElements(currentVo.getList());
                        if (!CollectionUtils.isEmpty(currentVo.getList()) && batch.isEmpty()) {
                            throw new ServiceException("导出数据存在空行，请检查查询结果（独立分页 sheetIndex=" + i + "，页码=" + currPage + "）");
                        }
                        if (!CollectionUtils.isEmpty(batch)) {
                            fillAcrossSheets(excelWriter, fillConfig, batch, cursors[i], i, "INDEPENDENT");
                            writtenForType += batch.size();
                            if (i == 0) {
                                actualMainRows += batch.size();
                            }
                        }
                        // 上游单页超发时已写满本类型 totalCount：提前结束，避免预取下一页取到空列表被下方守卫误判缺数。
                        if (writtenForType >= totalRows[i] || pageOffset == totalPages - 1) {
                            break;
                        }
                        PagingDTO<P> pageDto = pagingDtos[i];
                        pageDto.setCurrPage(currPage + 1);
                        PagingVO<?> nextVo = sheets.get(i).getPageFetcher().apply(pageDto);
                        // 后续页返回 null 视为分页查询失败，显式抛错，避免以不完整数据"成功"结束导致静默丢数
                        if (nextVo == null) {
                            throw new ServiceException("独立分页导出后续页返回为空，sheetIndex=" + i + "，页码=" + (currPage + 1));
                        }
                        if (CollectionUtils.isEmpty(nextVo.getList())) {
                            // totalCount 估算仍有后续页，但实际返回空列表：属分页查询异常或数据并发变更，
                            // 若仅 break 会以不完整 Excel"成功"结束、fileTask.count 仅反映已写行数，用户难感知缺数，故显式抛错。
                            throw new ServiceException("独立分页导出数据缺失：sheetIndex=" + i + "，页码=" + (currPage + 1)
                                    + " 实际返回空列表但 totalCount=" + totalRows[i] + " 仍有后续页，疑似分页查询异常或数据并发变更，请重试或联系开发排查上游分页接口。");
                        }
                        currentVo = nextVo;
                    }
                    if (totalRows[i] > 0 && i == 0 && actualMainRows == 0) {
                        throw new ServiceException("导出失败：主 sheet totalCount=" + totalRows[0]
                                + " 但未写入任何数据行，疑似分页查询异常或数据全为空行，请检查上游分页接口。");
                    }
                }
            } finally {
                excelWriter.finish();
            }
        }
        // count 取首个（主）sheet 实际写入行数（非 totalCount），与 streamMasterDerived 及 writeAllSheets 文档语义一致；
        // 不返回各 sheet 行数之和，避免 fileTask.count 被放大影响任务展示/下游统计。
        // 独立多 sheet 约定 sheets 列表首项为主表（typeCount>=1，空集合在方法开头已抛异常）。
        return actualMainRows;
    }

    /**
     * 主从派生导出：仅分页查询主表，明细行由 {@code sheetExtractors} 从主表对象内存派生（非独立分页查询）。
     * <p>
     * 主要用于兼容历史单据导出，历史单据类型有限、单条主表行派生明细量可控，
     * 故明细 sheet 与主表 sheet 保持 1:1 绑定、明细不跨物理 sheet 续写；
     * 仅当单个主表 sheet 内派生明细累计超过 Excel 单 sheet 上限（约 104 万行）这一极端场景才显式抛错，
     * 不为该小概率场景引入「明细独立扩容/跨 sheet 续写」的复杂度。
     */
    public <P, M> int streamMasterDerived(File outFile,
                                          MasterDerivedSpec<P, M> spec,
                                          int pageSize,
                                          int firstPage) throws IOException {
        if (spec == null || spec.getMasterPageFetcher() == null) {
            throw new ServiceException("主从派生导出配置不能为空");
        }
        if (CollectionUtils.isEmpty(spec.getSheetExtractors())) {
            throw new ServiceException("主从派生 sheetExtractors 不能为空");
        }
        int typeCount = spec.getSheetExtractors().size();
        if (typeCount <= 0) {
            throw new ServiceException("主从派生 sheetExtractors 不能为空");
        }

        PagingDTO<P> pagingDto = newPagingDTO(spec.getParams(), pageSize, firstPage);
        PagingVO<M> firstVo = spec.getMasterPageFetcher().apply(pagingDto);
        if (firstVo == null) {
            throw new ServiceException("主从派生主分页返回为空");
        }
        int total = Math.max(0, firstVo.getTotalCount());
        int mainSheetCount = computeConfiguredSheetCount(total);

        int[] dataSheetCountPerType = new int[typeCount];
        int[] rowLimitPerType = new int[typeCount];
        dataSheetCountPerType[0] = mainSheetCount;
        rowLimitPerType[0] = Math.min(maxDataRowsPerSheet, maxRowsPerXlsxSheetHardLimit);
        for (int i = 1; i < typeCount; i++) {
            dataSheetCountPerType[i] = mainSheetCount;
            rowLimitPerType[i] = maxRowsPerXlsxSheetHardLimit;
        }

        ExpandedTemplate expanded = expandTemplate(readClasspathTemplateBytes(excelPath), dataSheetCountPerType);
        SheetCursor[] cursors = buildCursors(expanded.sheetIndexesPerType, rowLimitPerType);
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
        WriteHandler[] handlers = writeHandlers.toArray(new WriteHandler[0]);

        int actualMainRows = 0;
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expanded.templateBytes, handlers);
            try {
                int totalPages = computeTotalPages(total, pageSize);
                PagingVO<M> currentVo = firstVo;
                for (int pageOffset = 0; pageOffset < totalPages; pageOffset++) {
                    int currPage = firstPage + pageOffset;
                    // 当前页（含首页）空列表防静默丢数：本页之前已覆盖行数 < totalCount 说明本页本应有数据，
                    // 实际返回空属上游分页异常/数据并发变更；与 OFFSET 路径（AbstractPageFileEventHandler.writeOffsetBatches）守卫对齐。
                    long coveredBefore = (long) pageOffset * pageSize;
                    if (CollectionUtils.isEmpty(currentVo.getList()) && coveredBefore < total) {
                        throw new ServiceException("主从派生导出数据缺失：页码=" + currPage
                                + " 返回空列表，但 totalCount=" + total + " 预期仍有数据，疑似分页查询异常或数据并发变更，请重试或联系开发排查上游分页接口。");
                    }
                    List<M> mainBatch = withoutNullListElements(currentVo.getList());
                    if (!CollectionUtils.isEmpty(currentVo.getList()) && mainBatch.isEmpty()) {
                        throw new ServiceException("导出数据存在空行，请检查查询结果（主从派生，页码=" + currPage + "）");
                    }
                    if (!CollectionUtils.isEmpty(mainBatch)) {
                        fillMasterDerivedBatch(excelWriter, fillConfig, mainBatch, cursors, spec.getSheetExtractors());
                        actualMainRows += mainBatch.size();
                    }
                    // 上游单页超发时已写满 totalCount：提前结束，避免预取下一页取到空列表被下方守卫误判缺数。
                    if (actualMainRows >= total || pageOffset == totalPages - 1) {
                        break;
                    }
                    pagingDto.setCurrPage(currPage + 1);
                    PagingVO<M> nextVo = spec.getMasterPageFetcher().apply(pagingDto);
                    // 后续页返回 null 视为分页查询失败，显式抛错，避免以不完整数据"成功"结束导致静默丢数
                    if (nextVo == null) {
                        throw new ServiceException("主从派生导出后续页返回为空，页码=" + (currPage + 1));
                    }
                    if (CollectionUtils.isEmpty(nextVo.getList())) {
                        // totalCount 估算仍有后续页，但实际返回空列表：属分页查询异常或数据并发变更，
                        // 若仅 break 会以不完整 Excel"成功"结束、fileTask.count 仅反映已写行数，用户难感知缺数，故显式抛错。
                        throw new ServiceException("主从派生导出数据缺失：页码=" + (currPage + 1)
                                + " 实际返回空列表但 totalCount=" + total + " 仍有后续页，疑似分页查询异常或数据并发变更，请重试或联系开发排查上游分页接口。");
                    }
                    currentVo = nextVo;
                }
                if (total > 0 && actualMainRows == 0) {
                    throw new ServiceException("导出失败：totalCount=" + total
                            + " 但未写入任何数据行，疑似分页查询异常或数据全为空行，请检查上游分页接口。");
                }
            } finally {
                excelWriter.finish();
            }
        }
        return actualMainRows;
    }

    private <M> void fillMasterDerivedBatch(ExcelWriter excelWriter,
                                            FillConfig fillConfig,
                                            List<M> mainBatch,
                                            SheetCursor[] cursors,
                                            List<Function<List<M>, List<?>>> extractors) {
        SheetCursor mainCursor = cursors[0];
        int idx = 0;
        while (idx < mainBatch.size()) {
            ensureCursorSheet(mainCursor);
            int room = mainCursor.rowLimit - (int) mainCursor.rowsInSheet;
            if (room <= 0) {
                mainCursor.sheetNo++;
                mainCursor.rowsInSheet = 0;
                ensureCursorSheet(mainCursor);
                room = mainCursor.rowLimit;
            }
            int take = Math.min(room, mainBatch.size() - idx);
            List<M> mainSlice = mainBatch.subList(idx, idx + take);

            List<?> mainRows = extractors.get(0).apply(mainSlice);
            List<?> sanitizedMainRows = withoutNullListElements(mainRows);
            if (sanitizedMainRows.size() != take) {
                throw new ServiceException("主sheet提取器返回行数必须与主分页切片行数一致");
            }
            fillCurrentSheet(excelWriter, fillConfig, sanitizedMainRows, mainCursor, 0, "MASTER");
            mainCursor.rowsInSheet += take;

            for (int typeIndex = 1; typeIndex < cursors.length; typeIndex++) {
                SheetCursor detailCursor = cursors[typeIndex];
                // 主 sheet 切换物理页后，明细 cursor 需跟随并将本 sheet 行计数归零，
                // 否则跨 sheet 累加会导致单段超限误判（与主 sheet fillAcrossSheets 的归零行为保持一致）。
                if (detailCursor.sheetNo != mainCursor.sheetNo) {
                    detailCursor.sheetNo = mainCursor.sheetNo;
                    detailCursor.rowsInSheet = 0;
                }
                ensureCursorSheet(detailCursor);
                List<?> detailRows = extractors.get(typeIndex).apply(mainSlice);
                List<?> sanitizedDetailRows = withoutNullListElements(detailRows);
                if (CollectionUtils.isEmpty(sanitizedDetailRows)) {
                    continue;
                }
                long nextRows = detailCursor.rowsInSheet + sanitizedDetailRows.size();
                if (nextRows > detailCursor.rowLimit) {
                    // 主从派生布局下明细 sheet 与主表 sheet 1:1 绑定，明细不跨物理 sheet 续写；
                    // 单个主表 sheet 内派生明细累计超过 Excel 单 sheet 上限时无法继续，明确失败而非静默截断。
                    throw new ServiceException("明细sheet[typeIndex=" + typeIndex + "]单段累计 " + nextRows
                            + " 行，超过Excel单sheet最大行数 " + detailCursor.rowLimit
                            + "（明细随主表分sheet、不跨sheet续写）。请缩小筛选范围或减少单主表行的明细展开量后重试。");
                }
                fillCurrentSheet(excelWriter, fillConfig, sanitizedDetailRows, detailCursor, typeIndex, "DETAIL_SEGMENT");
                detailCursor.rowsInSheet = nextRows;
            }
            idx += take;
        }
    }

    private void fillAcrossSheets(ExcelWriter excelWriter,
                                  FillConfig fillConfig,
                                  List<?> batch,
                                  SheetCursor cursor,
                                  int typeIndex,
                                  String phase) {
        int idx = 0;
        while (idx < batch.size()) {
            ensureCursorSheet(cursor);
            int room = cursor.rowLimit - (int) cursor.rowsInSheet;
            if (room <= 0) {
                cursor.sheetNo++;
                cursor.rowsInSheet = 0;
                ensureCursorSheet(cursor);
                room = cursor.rowLimit;
            }
            int take = Math.min(room, batch.size() - idx);
            List<?> slice = batch.subList(idx, idx + take);
            fillCurrentSheet(excelWriter, fillConfig, slice, cursor, typeIndex, phase);
            cursor.rowsInSheet += take;
            idx += take;
        }
    }

    private void fillCurrentSheet(ExcelWriter excelWriter,
                                  FillConfig fillConfig,
                                  List<?> rows,
                                  SheetCursor cursor,
                                  int typeIndex,
                                  String phase) {
        try {
            excelWriter.fill(rows, fillConfig, cursor.writeSheet);
        } catch (Throwable ex) {
            // initCause 保留原始 cause，避免线上仅见包装信息无法定位根因（ServiceException 无 cause 构造器，
            // 且新增 (String,Throwable) 重载会改变既有 (String,Object...) 调用方的占位符格式化行为，故此处用 initCause）
            ServiceException se = new ServiceException("多sheet模板填充失败，phase=" + phase + ",typeIndex=" + typeIndex + ",sheetNo=" + cursor.sheetNo);
            se.initCause(ex);
            throw se;
        }
    }

    private void ensureCursorSheet(SheetCursor cursor) {
        if (cursor.sheetNo < 0 || cursor.sheetNo >= cursor.dataSheetIndexes.size()) {
            throw new ServiceException("导出数据超过当前模板可承载的sheet数量，请缩小筛选范围导出。");
        }
        cursor.writeSheet = EasyExcel.writerSheet(cursor.dataSheetIndexes.get(cursor.sheetNo)).build();
    }

    private SheetCursor[] buildCursors(List<List<Integer>> indexesPerType, int[] rowLimitPerType) {
        SheetCursor[] cursors = new SheetCursor[indexesPerType.size()];
        for (int i = 0; i < indexesPerType.size(); i++) {
            SheetCursor cursor = new SheetCursor();
            cursor.sheetNo = 0;
            cursor.rowsInSheet = 0;
            cursor.dataSheetIndexes = indexesPerType.get(i);
            cursor.rowLimit = rowLimitPerType[i];
            ensureCursorSheet(cursor);
            cursors[i] = cursor;
        }
        return cursors;
    }

    private int computeConfiguredSheetCount(int totalRows) {
        if (totalRows <= 0) {
            return 1;
        }
        long count = (totalRows + (long) maxDataRowsPerSheet - 1) / (long) maxDataRowsPerSheet;
        if (count > maxTemplateDataSheets) {
            throw new ServiceException("导出约需 " + count + " 张数据表，超过系统上限 " + maxTemplateDataSheets + "，请缩小筛选范围导出。");
        }
        return (int) count;
    }

    private static int computeTotalPages(int totalCount, int pageSize) {
        if (pageSize <= 0) {
            throw new ServiceException("pageSize 必须大于0");
        }
        if (totalCount <= 0) {
            return 1;
        }
        return (int) ((totalCount + (long) pageSize - 1L) / (long) pageSize);
    }

    private static <P> PagingDTO<P> newPagingDTO(P params, int pageSize, int currPage) {
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(pageSize);
        dto.setCurrPage(currPage);
        dto.setParams(params);
        return dto;
    }

    private byte[] readClasspathTemplateBytes(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream in = resource.getInputStream()) {
            return IOUtils.toByteArray(in);
        }
    }

    private ExpandedTemplate expandTemplate(byte[] templateBytes, int[] sheetCountPerType) throws IOException {
        if (sheetCountPerType == null || sheetCountPerType.length == 0) {
            throw new ServiceException("sheetCountPerType 不能为空");
        }
        // 与单 sheet 路径（AbstractPageFileEventHandler.expandTemplateWithDataSheetCopies）一致的内存上界保护：
        // POI 整本克隆全部类型 sheet 后再整本写出，峰值内存与「模板字节 × 总展开 sheet 数」正相关，
        // 以 file.storage.maxTemplateExpandBytes（默认 300MB）做固定上界、早失败避免 OOM。
        long totalExpandSheets = 0L;
        for (int c : sheetCountPerType) {
            totalExpandSheets += Math.max(0, c);
        }
        long expandFootprint = (long) templateBytes.length * totalExpandSheets;
        long maxExpandBytes = FileRegistry.maxTemplateExpandBytesOrDefault();
        if (expandFootprint > maxExpandBytes) {
            throw new ServiceException("多sheet导出模板展开预估占用过大（模板≈" + (templateBytes.length / 1024)
                    + "KB × " + totalExpandSheets + " 张 ≈ " + (expandFootprint / 1024 / 1024) + "MB，上限 "
                    + (maxExpandBytes / 1024 / 1024) + "MB），请简化模板、缩小导出范围或调大 file.storage.maxTemplateExpandBytes。");
        }
        try (ByteArrayInputStream bin = new ByteArrayInputStream(templateBytes);
             XSSFWorkbook wb = new XSSFWorkbook(bin)) {
            int baseSheetCount = wb.getNumberOfSheets();
            if (baseSheetCount != sheetCountPerType.length) {
                throw new ServiceException("模板sheet数量与实现声明不一致：模板=" + baseSheetCount + "，实现=" + sheetCountPerType.length);
            }

            List<String> baseSheetNames = new ArrayList<>(baseSheetCount);
            for (int i = 0; i < baseSheetCount; i++) {
                baseSheetNames.add(wb.getSheetName(i));
            }
            List<List<String>> namesPerType = new ArrayList<>(baseSheetCount);
            for (int i = 0; i < baseSheetCount; i++) {
                int count = sheetCountPerType[i];
                if (count <= 0) {
                    throw new ServiceException("sheetCountPerType[" + i + "] 必须大于0");
                }
                if (count > maxTemplateDataSheets) {
                    throw new ServiceException("sheetCountPerType[" + i + "] 超过 maxTemplateDataSheets 限制");
                }
                String baseName = baseSheetNames.get(i);
                int sourceIndex = wb.getSheetIndex(baseName);
                List<String> names = new ArrayList<>(count);
                names.add(baseName);
                for (int suffix = 1; suffix < count; suffix++) {
                    wb.cloneSheet(sourceIndex);
                    int cloneIndex = wb.getNumberOfSheets() - 1;
                    String cloneName = buildCloneName(wb, baseName, suffix);
                    wb.setSheetName(cloneIndex, cloneName);
                    wb.setSheetOrder(cloneName, sourceIndex + suffix);
                    names.add(cloneName);
                }
                namesPerType.add(names);
            }

            List<List<Integer>> indexesPerType = new ArrayList<>(namesPerType.size());
            for (List<String> names : namesPerType) {
                List<Integer> indexes = new ArrayList<>(names.size());
                for (String name : names) {
                    indexes.add(wb.getSheetIndex(name));
                }
                indexesPerType.add(indexes);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                wb.write(out);
                return new ExpandedTemplate(out.toByteArray(), indexesPerType);
            }
        }
    }

    private String buildCloneName(XSSFWorkbook wb, String baseName, int suffix) {
        String candidate = baseName + "_" + suffix;
        if (wb.getSheet(candidate) == null) {
            return candidate;
        }
        int idx = suffix;
        while (wb.getSheet(baseName + "_" + idx) != null) {
            idx++;
        }
        return baseName + "_" + idx;
    }

    @SuppressWarnings("unchecked")
    private static <E> List<E> withoutNullListElements(List<?> raw) {
        if (CollectionUtils.isEmpty(raw)) {
            return Collections.emptyList();
        }
        int nullCount = 0;
        for (Object row : raw) {
            if (row == null) {
                nullCount++;
            }
        }
        if (nullCount == 0) {
            return (List<E>) raw;
        }
        List<E> copy = new ArrayList<>(raw.size() - nullCount);
        for (Object row : raw) {
            if (row != null) {
                copy.add((E) row);
            }
        }
        return copy;
    }

    private static final class ExpandedTemplate {
        private final byte[] templateBytes;
        private final List<List<Integer>> sheetIndexesPerType;

        private ExpandedTemplate(byte[] templateBytes, List<List<Integer>> sheetIndexesPerType) {
            this.templateBytes = templateBytes;
            this.sheetIndexesPerType = sheetIndexesPerType;
        }
    }

    private static final class SheetCursor {
        private int sheetNo;
        private long rowsInSheet;
        private int rowLimit;
        private WriteSheet writeSheet;
        private List<Integer> dataSheetIndexes;
    }

    @Getter
    public static final class IndependentSheet<P> {
        private final P params;
        private final Function<PagingDTO<P>, PagingVO<?>> pageFetcher;

        public IndependentSheet(P params, Function<PagingDTO<P>, PagingVO<?>> pageFetcher) {
            this.params = params;
            this.pageFetcher = Objects.requireNonNull(pageFetcher, "pageFetcher");
        }

    }

    @Getter
    public static final class MasterDerivedSpec<P, M> {
        private final P params;
        private final Function<PagingDTO<P>, PagingVO<M>> masterPageFetcher;
        private final List<Function<List<M>, List<?>>> sheetExtractors;

        public MasterDerivedSpec(P params,
                                 Function<PagingDTO<P>, PagingVO<M>> masterPageFetcher,
                                 List<Function<List<M>, List<?>>> sheetExtractors) {
            this.params = params;
            this.masterPageFetcher = Objects.requireNonNull(masterPageFetcher, "masterPageFetcher");
            this.sheetExtractors = Objects.requireNonNull(sheetExtractors, "sheetExtractors");
        }

    }
}
