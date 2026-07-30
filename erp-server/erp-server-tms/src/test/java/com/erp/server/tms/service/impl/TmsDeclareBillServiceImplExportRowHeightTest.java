package com.erp.server.tms.service.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 报关单导出明细行高计算测试。
 */
public class TmsDeclareBillServiceImplExportRowHeightTest {

    private static final int TEMPLATE_ROW_INDEX = 17;
    private static final short TEMPLATE_ROW_HEIGHT = 300;
    private TmsDeclareBillServiceImpl service;

    @Before
    public void setUp() {
        service = new TmsDeclareBillServiceImpl();
    }

    /**
     * 验证模板第18行高度作为短文本明细的最小行高。
     */
    @Test
    public void shortTextNeverFallsBelowTemplateHeight() throws IOException {
        try (XSSFWorkbook workbook = workbookWithTemplateRow()) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.setColumnWidth(3, 20 * 256);
            Row detailRow = sheet.getRow(TEMPLATE_ROW_INDEX);
            detailRow.getCell(3).setCellValue("短文本");

            invokeRowHeightCalculation(sheet, 1);

            assertEquals(TEMPLATE_ROW_HEIGHT, detailRow.getHeight());
        }
    }

    /**
     * 验证长文本和显式换行会使明细行自然增高。
     */
    @Test
    public void longTextAndExplicitLineBreakIncreaseHeight() throws IOException {
        try (XSSFWorkbook workbook = workbookWithTemplateRow()) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.setColumnWidth(3, 4 * 256);
            Row longRow = sheet.getRow(TEMPLATE_ROW_INDEX);
            longRow.getCell(3).setCellValue("这是一个需要根据列宽自动换行的较长申报要素内容");
            Row breakRow = createDetailRow(sheet, TEMPLATE_ROW_INDEX + 1);
            breakRow.getCell(3).setCellValue("第一行\n第二行");

            invokeRowHeightCalculation(sheet, 2);

            assertTrue(longRow.getHeight() > TEMPLATE_ROW_HEIGHT);
            assertTrue(breakRow.getHeight() > TEMPLATE_ROW_HEIGHT);
        }
    }

    /**
     * 验证横向合并区域使用总宽度且同一明细行各字段取最大高度而不是相加。
     */
    @Test
    public void mergedWidthIsCountedOnceAndRowUsesLargestField() throws IOException {
        try (XSSFWorkbook workbook = workbookWithTemplateRow()) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int column = 3; column <= 5; column++) {
                sheet.setColumnWidth(column, 4 * 256);
            }
            sheet.addMergedRegion(new CellRangeAddress(TEMPLATE_ROW_INDEX, TEMPLATE_ROW_INDEX, 3, 5));
            Row detailRow = sheet.getRow(TEMPLATE_ROW_INDEX);
            detailRow.getCell(3).setCellValue("合并字段的文本应按三列总宽度换行");
            detailRow.createCell(6).setCellValue("普通字段的长文本也需要换行");
            sheet.setColumnWidth(6, 4 * 256);

            int mergedOnlyHeight;
            try (XSSFWorkbook mergedOnlyWorkbook = workbookWithTemplateRow()) {
                Sheet mergedOnlySheet = mergedOnlyWorkbook.getSheetAt(0);
                for (int column = 3; column <= 5; column++) {
                    mergedOnlySheet.setColumnWidth(column, 4 * 256);
                }
                mergedOnlySheet.addMergedRegion(new CellRangeAddress(TEMPLATE_ROW_INDEX, TEMPLATE_ROW_INDEX, 3, 5));
                mergedOnlySheet.getRow(TEMPLATE_ROW_INDEX).getCell(3).setCellValue("合并字段的文本应按三列总宽度换行");
                invokeRowHeightCalculation(mergedOnlySheet, 1);
                mergedOnlyHeight = mergedOnlySheet.getRow(TEMPLATE_ROW_INDEX).getHeight();
            }

            invokeRowHeightCalculation(sheet, 1);

            int normalFieldOnlyHeight;
            try (XSSFWorkbook normalOnlyWorkbook = workbookWithTemplateRow()) {
                Sheet normalOnlySheet = normalOnlyWorkbook.getSheetAt(0);
                normalOnlySheet.setColumnWidth(6, 4 * 256);
                normalOnlySheet.getRow(TEMPLATE_ROW_INDEX).getCell(6)
                        .setCellValue("普通字段的长文本也需要换行");
                invokeRowHeightCalculation(normalOnlySheet, 1);
                normalFieldOnlyHeight = normalOnlySheet.getRow(TEMPLATE_ROW_INDEX).getHeight();
            }

            int expectedMaxHeight = Math.max(mergedOnlyHeight, normalFieldOnlyHeight);
            int heightIfAdded = mergedOnlyHeight + normalFieldOnlyHeight;
            assertEquals(expectedMaxHeight, detailRow.getHeight());
            assertTrue(detailRow.getHeight() < heightIfAdded);
        }
    }

    /**
     * 验证列宽读取异常时回退到模板默认列宽，而不是固定字符宽度。
     */
    @Test
    public void columnWidthFallbackUsesTemplateDefaultWidth() {
        Sheet sheet = Mockito.mock(Sheet.class);
        when(sheet.getColumnWidth(3)).thenThrow(new IllegalArgumentException("invalid column"));
        when(sheet.getDefaultColumnWidth()).thenReturn(12);

        Integer width = ReflectionTestUtils.invokeMethod(service, "resolveColumnWidth", sheet, 3);

        assertEquals(Integer.valueOf(12 * 256), width);
    }

    /**
     * 验证明细文本单元格会启用自动换行，避免行高增加后内容仍无法折行展示。
     */
    @Test
    public void detailTextCellEnablesWrapText() throws IOException {
        try (XSSFWorkbook workbook = workbookWithTemplateRow()) {
            Sheet sheet = workbook.getSheetAt(0);
            Row detailRow = sheet.getRow(TEMPLATE_ROW_INDEX);
            Cell cell = detailRow.getCell(6);
            cell.setCellValue("较长的境内货源地内容");
            assertTrue(!cell.getCellStyle().getWrapText());

            invokeRowHeightCalculation(sheet, 1);

            assertTrue(cell.getCellStyle().getWrapText());
        }
    }

    /**
     * 验证模板41磅是整行最小高度，不会因文本估算为两行而直接翻倍为82磅。
     */
    @Test
    public void templateHeightIsMinimumInsteadOfPerLineHeight() throws IOException {
        try (XSSFWorkbook workbook = workbookWithTemplateRow((short) 820)) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.setColumnWidth(3, 8 * 256);
            Row detailRow = sheet.getRow(TEMPLATE_ROW_INDEX);
            detailRow.getCell(3).setCellValue("123456789");

            invokeRowHeightCalculation(sheet, 1);

            assertEquals(820, detailRow.getHeight());
        }
    }

    /**
     * 验证内容超过模板容量时，按 sheet 默认单行高度增长，与 WPS 最适合行高一致。
     */
    @Test
    public void overflowingTextUsesSheetDefaultLineHeight() throws IOException {
        try (XSSFWorkbook workbook = workbookWithTemplateRow((short) 820)) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.setDefaultRowHeight((short) 375);
            sheet.setColumnWidth(3, 8 * 256);
            Row detailRow = sheet.getRow(TEMPLATE_ROW_INDEX);
            detailRow.getCell(3).setCellValue("1234567890123456789012345678901234567890");

            invokeRowHeightCalculation(sheet, 1);

            assertEquals(1875, detailRow.getHeight());
        }
    }

    private XSSFWorkbook workbookWithTemplateRow() {
        return workbookWithTemplateRow(TEMPLATE_ROW_HEIGHT);
    }

    private XSSFWorkbook workbookWithTemplateRow(short rowHeight) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("报关单");
        Row templateRow = sheet.createRow(TEMPLATE_ROW_INDEX);
        templateRow.setHeight(rowHeight);
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Cell templateCell = templateRow.createCell(3);
        templateCell.setCellStyle(wrapStyle);
        templateRow.createCell(6).setCellStyle(wrapStyle);
        return workbook;
    }

    private Row createDetailRow(Sheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(3).setCellStyle(sheet.getRow(TEMPLATE_ROW_INDEX).getCell(3).getCellStyle());
        row.createCell(6).setCellStyle(sheet.getRow(TEMPLATE_ROW_INDEX).getCell(6).getCellStyle());
        return row;
    }

    private void invokeRowHeightCalculation(Sheet sheet, int detailSize) {
        ReflectionTestUtils.invokeMethod(service, "setDeclareDetailRowHeights", sheet, detailSize);
    }
}
