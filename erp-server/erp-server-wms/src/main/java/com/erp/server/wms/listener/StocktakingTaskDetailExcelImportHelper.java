package com.erp.server.wms.listener;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.excel.StocktakingTaskDetailExcelDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.StocktakingProfitLossService;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.erp.server.wms.service.StocktakingTaskService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 盘点任务明细导入：支持按库区多 sheet、首行元信息；兼容旧版单 sheet。
 * <p>使用临时文件避免整文件常驻内存。</p>
 */
public final class StocktakingTaskDetailExcelImportHelper {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /** 导入文件大小上限：50MB */
    private static final long MAX_IMPORT_FILE_BYTES = 50L * 1024 * 1024;

    /** 导入 sheet 数上限 */
    private static final int MAX_IMPORT_SHEET_COUNT = 100;

    private StocktakingTaskDetailExcelImportHelper() {
    }

    /**
     * 详情页导入（已限定任务单号与明细列表）。
     */
    public static List<StocktakingTaskDetailExcelDTO> importDetailSheets(
            MultipartFile excelFile,
            StocktakingTaskService stocktakingTaskService,
            StocktakingTaskDetailService stocktakingTaskDetailService,
            StocktakingProfitLossService stocktakingProfitLossService,
            String taskCode,
            List<StocktakingTaskDetailEntity> taskDetailList,
            WarehouseService warehouseService,
            OperateLogService operateLogService) throws IOException {
        StocktakingTaskDetailExcelListener listener = new StocktakingTaskDetailExcelListener(
                stocktakingTaskService, stocktakingTaskDetailService, stocktakingProfitLossService,
                taskCode, taskDetailList, warehouseService, operateLogService);
        return importWithTempFile(excelFile, listener, listener::flush, listener::getErrorList);
    }

    /**
     * 列表页导入（按行内盘点任务单号匹配）。
     */
    public static List<StocktakingTaskDetailExcelDTO> importListSheets(
            MultipartFile excelFile,
            StocktakingTaskService stocktakingTaskService,
            StocktakingTaskDetailService stocktakingTaskDetailService,
            StocktakingProfitLossService stocktakingProfitLossService,
            WarehouseService warehouseService,
            OperateLogService operateLogService) throws IOException {
        StocktakingTaskExcelListener listener = new StocktakingTaskExcelListener(
                stocktakingTaskService, stocktakingTaskDetailService, stocktakingProfitLossService,
                warehouseService, operateLogService);
        return importWithTempFile(excelFile, listener, listener::flush, listener::getErrorList);
    }

    private static List<StocktakingTaskDetailExcelDTO> importWithTempFile(
            MultipartFile excelFile,
            AnalysisEventListener<StocktakingTaskDetailExcelDTO> listener,
            Runnable flushAction,
            Supplier<List<StocktakingTaskDetailExcelDTO>> errorSupplier) throws IOException {
        assertFileSize(excelFile);
        File tempFile = File.createTempFile("stocktaking-import-", ".xlsx");
        try {
            excelFile.transferTo(tempFile);
            List<SheetHeadInfo> sheetInfos = detectSheetHeadRows(tempFile);
            if (sheetInfos.size() > MAX_IMPORT_SHEET_COUNT) {
                throw new ServiceException("导入 sheet 数量 " + sheetInfos.size()
                        + " 超过上限 " + MAX_IMPORT_SHEET_COUNT + "，请拆分后导入。");
            }
            for (SheetHeadInfo info : sheetInfos) {
                try (InputStream in = new FileInputStream(tempFile)) {
                    EasyExcel.read(in, StocktakingTaskDetailExcelDTO.class, listener)
                            .headRowNumber(info.headRowNumber)
                            .sheet(info.sheetNo)
                            .doRead();
                }
            }
            flushAction.run();
            return errorSupplier.get();
        } finally {
            FileUtil.del(tempFile);
        }
    }

    private static void assertFileSize(MultipartFile excelFile) {
        long size = excelFile.getSize();
        if (size > MAX_IMPORT_FILE_BYTES) {
            throw new ServiceException("导入文件大小超过上限 "
                    + (MAX_IMPORT_FILE_BYTES / 1024 / 1024) + "MB，请拆分后导入。");
        }
    }

    private static List<SheetHeadInfo> detectSheetHeadRows(File file) throws IOException {
        List<SheetHeadInfo> result = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            int sheetCount = workbook.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }
                result.add(new SheetHeadInfo(i, detectHeadRowNumber(sheet)));
            }
        }
        return result;
    }

    /**
     * 新样式首行为「库区：…」，表头在第 2 行 → headRowNumber=2；
     * 旧样式首行即为「盘点任务单号」→ headRowNumber=1。
     */
    private static int detectHeadRowNumber(Sheet sheet) {
        Row row0 = sheet.getRow(0);
        if (row0 == null) {
            return 1;
        }
        String cell0 = cellString(row0.getCell(0));
        if (cell0.contains("盘点任务单号")) {
            return 1;
        }
        if (cell0.contains("库区") || cell0.contains("盘点人") || cell0.contains("仓管员") || cell0.contains("仓库主管")) {
            return 2;
        }
        int lastRow = Math.min(sheet.getLastRowNum(), 5);
        for (int r = 0; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (Cell cell : row) {
                String v = cellString(cell);
                if (v.contains("盘点任务单号") || v.contains("*盘点库存")) {
                    return r + 1;
                }
            }
        }
        return 1;
    }

    private static String cellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private static final class SheetHeadInfo {
        private final int sheetNo;
        private final int headRowNumber;

        private SheetHeadInfo(int sheetNo, int headRowNumber) {
            this.sheetNo = sheetNo;
            this.headRowNumber = headRowNumber;
        }
    }
}
