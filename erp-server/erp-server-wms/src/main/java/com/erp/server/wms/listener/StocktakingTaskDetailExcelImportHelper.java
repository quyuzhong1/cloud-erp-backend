package com.erp.server.wms.listener;

import com.alibaba.excel.EasyExcel;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 盘点任务明细导入：支持按库区多 sheet、首行元信息；兼容旧版单 sheet。
 */
public final class StocktakingTaskDetailExcelImportHelper {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

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
        byte[] bytes = excelFile.getBytes();
        List<SheetHeadInfo> sheetInfos = detectSheetHeadRows(bytes);
        StocktakingTaskDetailExcelListener sharedListener = new StocktakingTaskDetailExcelListener(
                stocktakingTaskService, stocktakingTaskDetailService, stocktakingProfitLossService,
                taskCode, taskDetailList, warehouseService, operateLogService);
        for (SheetHeadInfo info : sheetInfos) {
            EasyExcel.read(new ByteArrayInputStream(bytes), StocktakingTaskDetailExcelDTO.class, sharedListener)
                    .headRowNumber(info.headRowNumber)
                    .sheet(info.sheetNo)
                    .doRead();
        }
        sharedListener.flush();
        return sharedListener.getErrorList();
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
        byte[] bytes = excelFile.getBytes();
        List<SheetHeadInfo> sheetInfos = detectSheetHeadRows(bytes);
        StocktakingTaskExcelListener sharedListener = new StocktakingTaskExcelListener(
                stocktakingTaskService, stocktakingTaskDetailService, stocktakingProfitLossService,
                warehouseService, operateLogService);
        for (SheetHeadInfo info : sheetInfos) {
            EasyExcel.read(new ByteArrayInputStream(bytes), StocktakingTaskDetailExcelDTO.class, sharedListener)
                    .headRowNumber(info.headRowNumber)
                    .sheet(info.sheetNo)
                    .doRead();
        }
        sharedListener.flush();
        return sharedListener.getErrorList();
    }

    private static List<SheetHeadInfo> detectSheetHeadRows(byte[] bytes) throws IOException {
        List<SheetHeadInfo> result = new ArrayList<>();
        try (InputStream in = new ByteArrayInputStream(bytes); Workbook workbook = WorkbookFactory.create(in)) {
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
