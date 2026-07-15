package com.erp.server.wms.listener;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.erp.server.wms.service.impl.StocktakingTaskDetailServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 盘点任务明细导入模板（按库区 sheet + 首行元信息）。
 */
public final class StocktakingTaskDetailExcelTemplateWriter {

    private static final List<List<String>> DETAIL_HEAD = Arrays.asList(
            Collections.singletonList("盘点任务单号"),
            Collections.singletonList("盘点方式"),
            Collections.singletonList("仓库"),
            Collections.singletonList("仓位"),
            Collections.singletonList("SKU"),
            Collections.singletonList("产品名称"),
            Collections.singletonList("可用库存"),
            Collections.singletonList("冻结库存"),
            Collections.singletonList("初盘数量"),
            Collections.singletonList("*盘点库存"),
            Collections.singletonList("差异数量"),
            Collections.singletonList("盘点人")
    );

    private StocktakingTaskDetailExcelTemplateWriter() {
    }

    /**
     * 写出空模板：一个「空仓位」示例 sheet，首行元信息 + 表头，无数据行。
     */
    public static void writeEmptyTemplate(OutputStream outputStream) {
        String areaName = StocktakingTaskDetailServiceImpl.EMPTY_WAREHOUSE_AREA_NAME;
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(outputStream).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(0, areaName)
                    .head(DETAIL_HEAD)
                    .relativeHeadRowIndex(1)
                    .registerWriteHandler(new MetaHandler(areaName))
                    .build();
            excelWriter.write(Collections.emptyList(), writeSheet);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private static class MetaHandler implements SheetWriteHandler {
        private final String areaName;

        private MetaHandler(String areaName) {
            this.areaName = areaName;
        }

        @Override
        public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            // no-op
        }

        @Override
        public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            Sheet sheet = writeSheetHolder.getSheet();
            Row row = sheet.getRow(0);
            if (row == null) {
                row = sheet.createRow(0);
            }
            row.createCell(0).setCellValue("库区：");
            row.createCell(1).setCellValue(areaName);
            row.createCell(2).setCellValue("盘点人：");
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("仓管员：");
            row.createCell(5).setCellValue("");
            row.createCell(6).setCellValue("仓库主管：");
            row.createCell(7).setCellValue("");
            applyColumnWidths(sheet);
        }
    }

    /**
     * 与导出 Handler 保持一致的明细列宽。
     */
    private static void applyColumnWidths(Sheet sheet) {
        int[] charWidths = {20, 12, 18, 16, 22, 30, 12, 12, 12, 12, 12, 18};
        for (int i = 0; i < charWidths.length; i++) {
            sheet.setColumnWidth(i, charWidths[i] * 256);
        }
    }
}
