package com.erp.server.file.core;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.erp.server.file.exception.BusinessException;
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

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expanded.templateBytes, handlers);
            try {
                for (int i = 0; i < typeCount; i++) {
                    int totalPages = computeTotalPages(totalRows[i], pageSize);
                    PagingVO<?> currentVo = firstPages[i];
                    for (int pageOffset = 0; pageOffset < totalPages; pageOffset++) {
                        int currPage = firstPage + pageOffset;
                        List<?> batch = withoutNullListElements(currentVo.getList());
                        if (!CollectionUtils.isEmpty(batch)) {
                            fillAcrossSheets(excelWriter, fillConfig, batch, cursors[i], i, "INDEPENDENT");
                        }
                        if (pageOffset == totalPages - 1) {
                            break;
                        }
                        PagingDTO<P> pageDto = pagingDtos[i];
                        pageDto.setCurrPage(currPage + 1);
                        PagingVO<?> nextVo = sheets.get(i).getPageFetcher().apply(pageDto);
                        if (nextVo == null || CollectionUtils.isEmpty(nextVo.getList())) {
                            break;
                        }
                        currentVo = nextVo;
                    }
                }
            } finally {
                excelWriter.finish();
            }
        }
        return totalRows[0];
    }

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

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            ExcelWriter excelWriter = ExcelPrintUtils.openTemplateListWriter(fos, expanded.templateBytes, handlers);
            try {
                int totalPages = computeTotalPages(total, pageSize);
                PagingVO<M> currentVo = firstVo;
                for (int pageOffset = 0; pageOffset < totalPages; pageOffset++) {
                    int currPage = firstPage + pageOffset;
                    List<M> mainBatch = withoutNullListElements(currentVo.getList());
                    if (!CollectionUtils.isEmpty(mainBatch)) {
                        fillMasterDerivedBatch(excelWriter, fillConfig, mainBatch, cursors, spec.getSheetExtractors());
                    }
                    if (pageOffset == totalPages - 1) {
                        break;
                    }
                    pagingDto.setCurrPage(currPage + 1);
                    PagingVO<M> nextVo = spec.getMasterPageFetcher().apply(pagingDto);
                    if (nextVo == null || CollectionUtils.isEmpty(nextVo.getList())) {
                        break;
                    }
                    currentVo = nextVo;
                }
            } finally {
                excelWriter.finish();
            }
        }
        return total;
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
                detailCursor.sheetNo = mainCursor.sheetNo;
                ensureCursorSheet(detailCursor);
                List<?> detailRows = extractors.get(typeIndex).apply(mainSlice);
                List<?> sanitizedDetailRows = withoutNullListElements(detailRows);
                if (CollectionUtils.isEmpty(sanitizedDetailRows)) {
                    continue;
                }
                long nextRows = detailCursor.rowsInSheet + sanitizedDetailRows.size();
                if (nextRows > detailCursor.rowLimit) {
                    throw new BusinessException("明细sheet单段数据超过Excel单sheet最大行数（约104万），请缩小筛选范围导出。");
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
            throw new BusinessException("多sheet模板填充失败，phase=" + phase + ",typeIndex=" + typeIndex + ",sheetNo=" + cursor.sheetNo + ",msg=" + ex.getMessage());
        }
    }

    private void ensureCursorSheet(SheetCursor cursor) {
        if (cursor.sheetNo < 0 || cursor.sheetNo >= cursor.dataSheetIndexes.size()) {
            throw new BusinessException("导出数据超过当前模板可承载的sheet数量，请缩小筛选范围导出。");
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
            throw new BusinessException("导出约需 " + count + " 张数据表，超过系统上限 " + maxTemplateDataSheets + "，请缩小筛选范围导出。");
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
