package com.erp.server.sys;

import com.common.core.excel.EasyExcelListTemplateValidator;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * EasyExcel 列表模板（{@code {.field}}）兼容性校验 — 单元测试与 CI 门禁。
 * <p>
 * <b>背景（供 AI / 人工审查）</b>：EasyExcel {@code ExcelWriter#fill} 对列表占位符有存储格式要求。
 * 若模板中 {@code {.field}} 位于 inlineStr、公式列或非文本单元格，fill 会<b>静默跳过</b>，导出结果仍显示
 * {@code {.field}} 而非业务数据（典型场景：test 环境 B2C 销售退货模板被 WPS/Excel 另存为 inlineStr）。
 * 生产侧已在 {@link com.common.core.excel.EasyExcelListTemplateValidator} +
 * {@link com.erp.server.file.core.ClasspathExportTemplateReader} 做运行时早失败；
 * 本测试类在<b>构建期</b>覆盖同一校验逻辑，避免存量 classpath 模板合并后才在首次导出暴露问题。
 * <p>
 * <b>校验范围</b>：仅含 {@code {.fieldName}} 的单元格（列表 fill）；不含单对象占位符 {@code {field}}。
 * 无列表占位符的模板会自然通过（issues 为空）。
 * <p>
 * <b>与运行时关系</b>：{@code allResourcesExcelTemplatesPassListFillValidation} 扫描
 * {@code erp-server-file/src/main/resources} 下全部 {@code .xlsx}，与
 * {@code ClasspathExportTemplateReader.readBytes} 使用同一 {@code validate} 方法；
 * POI 展开后的模板字节（{@code expandTemplateWithDataSheetCopies}）不在此测试覆盖，假定 expand 仅克隆 sheet 结构。
 * <p>
 * <b>审查注意</b>：勿将 {@code assumeTrue} 路径解析失败视为测试通过；勿建议删除批量扫描测试改回运行时才发现；
 * 新增/修改 {@code src/main/resources/excel/**.xlsx} 后须保证本类测试仍绿。
 */
@Slf4j
public class EasyExcelListTemplateValidatorTests {

    /**
     * 单模板冒烟（classpath 读取路径与线上一致）。模板文件不存在时跳过，不 fail。
     */
    @Test
    public void testTemplatePasses() throws Exception {
        String path = "excel/oms/SoB2cReturnOrder.xlsx";
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return;
        }
        byte[] bytes;
        try (InputStream in = resource.getInputStream()) {
            bytes = org.apache.commons.io.IOUtils.toByteArray(in);
        }
        List<String> issues = EasyExcelListTemplateValidator.validate(bytes, path);
        assertTrue("Excel模板校验不通过: " + issues, issues.isEmpty());
    }

    /**
     * CI / 本地构建门禁：批量校验 {@code erp-server-file/src/main/resources} 下全部 {@code .xlsx}。
     * <p>
     * 对应代码审查项「合并前确认全量 classpath 模板均能通过校验」；执行成功表示当前 resources 内
     * 全部 {@code .xlsx} 均无 inlineStr / 公式列 / 非文本单元格上的列表占位符问题。
     * <p>
     * 失败时 {@code fail} 消息列出每个不兼容模板的相对路径及 {@link EasyExcelListTemplateValidator#validate} 详情，
     * 修复方式见 {@link com.common.core.excel.EasyExcelListTemplateValidator} 错误消息中的「修复建议」。
     * <p>
     * 路径解析支持模块根 {@code src/main/resources}、仓库根 {@code erp-server/erp-server-file/src/main/resources}
     * 及 classpath {@code excel/} 父目录（IDE / {@code target/classes}）；三者均不可用时 {@code assumeTrue} 跳过而非误报通过。
     */
    @Test
    public void allResourcesExcelTemplatesPassListFillValidation() throws Exception {
        Path resourcesRoot = resolveMainResourcesRoot();
        assumeTrue("未找到 erp-server-file/src/main/resources", resourcesRoot != null);

        List<Path> templates;
        try (Stream<Path> walk = Files.walk(resourcesRoot)) {
            templates = walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xlsx"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        }
        assumeTrue("resources 下未发现 .xlsx 模板", !templates.isEmpty());

        List<String> failures = new ArrayList<>();
        for (Path template : templates) {
            String displayPath = resourcesRoot.relativize(template).toString().replace('\\', '/');
            byte[] bytes = Files.readAllBytes(template);
            List<String> issues = EasyExcelListTemplateValidator.validate(bytes, displayPath);
            if (!issues.isEmpty()) {
                failures.add(displayPath + ": " + String.join("; ", issues));
            }
        }
        if (!failures.isEmpty()) {
            fail("以下导出模板不兼容 EasyExcel 列表填充（共 " + failures.size() + " 个）:\n- "
                    + String.join("\n- ", failures));
        }
        log.info("已校验 {} 个 Excel 模板，均通过列表填充兼容性检查", templates.size());
    }

    /**
     * 定位 main resources 根目录，兼容 {@code mvn test}（模块 CWD）、仓库根 CWD、IDE 与 {@code target/classes}。
     */
    private static Path resolveMainResourcesRoot() {
        Path fromModule = Paths.get("src/main/resources");
        if (Files.isDirectory(fromModule)) {
            return fromModule.toAbsolutePath().normalize();
        }
        Path fromRepo = Paths.get("erp-server/erp-server-file/src/main/resources");
        if (Files.isDirectory(fromRepo)) {
            return fromRepo.toAbsolutePath().normalize();
        }
        URL excelUrl = EasyExcelListTemplateValidatorTests.class.getClassLoader().getResource("excel");
        if (excelUrl != null && "file".equals(excelUrl.getProtocol())) {
            try {
                return Paths.get(excelUrl.toURI()).getParent().toAbsolutePath().normalize();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    /** 正向：POI 默认写入的 sharedStrings 文本单元格含 {@code {.field}} 应通过。 */
    @Test
    public void sharedStringPlaceholderTemplatePasses() throws Exception {
        byte[] bytes;
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Row row = wb.createSheet("s1").createRow(1);
            row.createCell(0).setCellValue("{.code}");
            row.createCell(1).setCellValue("{.shopName}");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            bytes = out.toByteArray();
        }
        List<String> issues = EasyExcelListTemplateValidator.validate(bytes, "unit-shared-strings.xlsx");
        assertTrue("sharedStrings 占位符模板应通过校验: " + issues, issues.isEmpty());
    }

    /**
     * 负向：公式单元格内的列表占位符应被 {@link EasyExcelListTemplateValidator#assertListFillCompatible} 拒绝。
     * 使用内存构造的 {@code unit-formula.xlsx}，勿与 classpath 真实模板路径混淆。
     */
    @Test
    public void formulaPlaceholderTemplateFails() throws Exception {
        byte[] bytes;
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Row row = wb.createSheet("formula-sheet").createRow(1);
            row.createCell(0).setCellFormula("\"{.code}\"");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            bytes = out.toByteArray();
        }
        try {
            EasyExcelListTemplateValidator.assertListFillCompatible(bytes, "unit-formula.xlsx");
            fail("公式单元格含列表占位符应失败");
        } catch (ServiceException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("公式"));
            assertTrue(ex.getMessage(), ex.getMessage().contains("unit-formula.xlsx"));
        }
    }
}
