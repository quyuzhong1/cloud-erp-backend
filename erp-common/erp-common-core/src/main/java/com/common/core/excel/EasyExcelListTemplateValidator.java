package com.common.core.excel;

import com.common.core.exception.ServiceException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EasyExcel 列表模板（{@code {.field}} + {@code ExcelWriter#fill}）兼容性校验。
 * <p>
 * 在导出加载 classpath 模板后尽早失败，避免 fill 静默留下未替换占位符。
 * 校验依据：EasyExcel 模板扫描会跳过公式/非纯文本单元格；inlineStr 等存储方式可能导致列表占位符无法被识别。
 * <p>
 * 构建期全量扫描见 {@code erp-server-file} 模块
 * {@code EasyExcelListTemplateValidatorTests#allResourcesExcelTemplatesPassListFillValidation}。
 */
public final class EasyExcelListTemplateValidator {

    private static final Pattern LIST_PLACEHOLDER = Pattern.compile("\\{\\.[a-zA-Z_][a-zA-Z0-9_]*\\}");
    private static final int MAX_REPORTED_ISSUES = 8;

    private EasyExcelListTemplateValidator() {
    }

    /**
     * 不兼容时抛出 {@link ServiceException}，消息包含模板路径与具体单元格定位。
     */
    public static void assertListFillCompatible(byte[] templateBytes, String templatePath) throws IOException {
        List<String> issues = validate(templateBytes, templatePath);
        if (issues.isEmpty()) {
            return;
        }
        throw new ServiceException(buildErrorMessage(templatePath, issues));
    }

    /**
     * 返回人类可读的问题描述（已去重、限量）。
     */
    public static List<String> validate(byte[] templateBytes, String templatePath) throws IOException {
        if (templateBytes == null || templateBytes.length == 0) {
            throw new ServiceException("导出模板为空（模板=" + templatePath + "）");
        }
        Set<String> issues = new LinkedHashSet<>();
        try (InputStream in = new ByteArrayInputStream(templateBytes);
             Workbook workbook = new XSSFWorkbook(in)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }
                scanSheet(sheet, formatter, evaluator, issues);
            }
        } catch (IOException ex) {
            throw new ServiceException("导出模板无法解析（模板=" + templatePath + "）：" + ex.getMessage());
        }
        return new ArrayList<>(issues);
    }

    private static void scanSheet(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator, Set<String> issues) {
        if (issues.size() >= MAX_REPORTED_ISSUES) {
            return;
        }
        String sheetName = sheet.getSheetName();
        for (Row row : sheet) {
            if (row == null || issues.size() >= MAX_REPORTED_ISSUES) {
                continue;
            }
            for (Cell cell : row) {
                if (cell == null || issues.size() >= MAX_REPORTED_ISSUES) {
                    continue;
                }
                String text = readCellText(cell, formatter, evaluator);
                if (text == null || !text.contains("{.")) {
                    continue;
                }
                collectPlaceholderIssues(sheetName, cell, text, issues);
            }
        }
    }

    private static void collectPlaceholderIssues(String sheetName, Cell cell, String text, Set<String> issues) {
        String cellRef = new CellReference(cell.getRowIndex(), cell.getColumnIndex()).formatAsString();
        Matcher matcher = LIST_PLACEHOLDER.matcher(text);
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            if (issues.size() >= MAX_REPORTED_ISSUES) {
                return;
            }
            String placeholder = matcher.group();
            String storageIssue = describeIncompatibleStorage(sheetName, cellRef, placeholder, cell);
            if (storageIssue != null) {
                issues.add(storageIssue);
            }
        }
        if (!matched && text.contains("{.")) {
            issues.add(describeLocation(sheetName, cellRef, null)
                    + "含 malformed 列表占位符（须为 {.fieldName}，fieldName 与 DTO getter 一致，不可含空格）");
        }
    }

    private static String describeIncompatibleStorage(String sheetName, String cellRef, String placeholder, Cell cell) {
        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            return describeNonStringStorageType(sheetName, cellRef, placeholder, cell);
        }
        if (cellType == CellType.FORMULA) {
            return describeLocation(sheetName, cellRef, placeholder)
                    + "位于公式单元格（EasyExcel 填充时会跳过公式列，占位符不会被替换）";
        }
        if (cellType == CellType.NUMERIC || cellType == CellType.BOOLEAN || cellType == CellType.ERROR) {
            return describeLocation(sheetName, cellRef, placeholder)
                    + "位于非文本单元格（列表占位符须为纯文本 sharedStrings 单元格）";
        }
        return null;
    }

    /**
     * 文本单元格上进一步检查 OOXML 存储类型（inlineStr 等）；sharedStrings 纯文本返回 null。
     */
    private static String describeNonStringStorageType(String sheetName, String cellRef, String placeholder, Cell cell) {
        if (!(cell instanceof XSSFCell)) {
            return null;
        }
        XSSFCell xssfCell = (XSSFCell) cell;
        if (!xssfCell.getCTCell().isSetT()) {
            return null;
        }
        STCellType.Enum storageType = xssfCell.getCTCell().getT();
        if (STCellType.INLINE_STR.equals(storageType)) {
            return describeLocation(sheetName, cellRef, placeholder)
                    + "使用 inlineStr 内联字符串（请基于兼容模板修改列，避免 WPS/Excel 另存后占位符变为 inlineStr）";
        }
        if (STCellType.N.equals(storageType)) {
            return describeLocation(sheetName, cellRef, placeholder)
                    + "位于数值单元格（列表占位符须为纯文本单元格）";
        }
        if (STCellType.B.equals(storageType)) {
            return describeLocation(sheetName, cellRef, placeholder)
                    + "位于布尔单元格（列表占位符须为纯文本单元格）";
        }
        if (STCellType.E.equals(storageType)) {
            return describeLocation(sheetName, cellRef, placeholder)
                    + "位于错误单元格（列表占位符须为纯文本单元格）";
        }
        return null;
    }

    private static String describeLocation(String sheetName, String cellRef, String placeholder) {
        if (placeholder == null) {
            return "sheet=" + sheetName + " " + cellRef + " ";
        }
        return "sheet=" + sheetName + " " + cellRef + " " + placeholder + " ";
    }

    private static String readCellText(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        try {
            return formatter.formatCellValue(cell, evaluator);
        } catch (Exception ex) {
            return cell.toString();
        }
    }

    private static String buildErrorMessage(String templatePath, List<String> issues) {
        StringBuilder sb = new StringBuilder();
        sb.append("导出模板不兼容 EasyExcel 列表填充（模板=").append(templatePath).append("）");
        int limit = Math.min(issues.size(), MAX_REPORTED_ISSUES);
        for (int i = 0; i < limit; i++) {
            sb.append("\n- ").append(issues.get(i));
        }
        if (issues.size() > limit) {
            sb.append("\n- ... 另有 ").append(issues.size() - limit).append(" 处类似问题");
        }
        sb.append("\n修复建议：列表占位符 {.field} 须放在纯文本 sharedStrings 单元格；")
                .append("勿在公式列写占位符；修改模板时请以 uat 兼容模板为底，避免 WPS/Excel 另存为 inlineStr；")
                .append("field 名须与导出 DTO 的 getter 一致。");
        return sb.toString();
    }
}
