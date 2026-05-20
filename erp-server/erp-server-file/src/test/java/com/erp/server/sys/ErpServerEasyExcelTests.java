package com.erp.server.sys;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.common.core.excel.ExcelPrintUtils;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.server.file.ErpServerFileApplication;
import com.erp.server.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerFileApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerEasyExcelTests {

    private static final String TEMPLATE_CLASSPATH = "excel/sys/city.xlsx";

    @Resource
    private FileService fileFeign;

    /**
     * 数据量大的复杂填充
     * <p>
     * 这里的解决方案是 确保模板list为最后一行，然后再拼接table.还有03版没救，只能刚正面加内存。
     *
     * @since 2.1.1
     */
    @Test
    public void complexFillWithTable() throws Exception {
        List<String> memoryReport = new ArrayList<>();
        snapshotMemory("1-测试开始", memoryReport);

        ClassPathResource templateResource = new ClassPathResource(TEMPLATE_CLASSPATH);
        Assume.assumeTrue(
                "缺少模板 " + TEMPLATE_CLASSPATH + "（请放到 erp-server-file/src/main/resources/ 下对应路径）",
                templateResource.exists());

        final byte[] templateBytes;
        try (InputStream in = templateResource.getInputStream()) {
            templateBytes = IOUtils.toByteArray(in);
        }
        snapshotMemory("2-模板读入内存(byte[])", memoryReport);

        File outFile = Files.createTempFile("complexFillWithTable-", ".xlsx").toFile();
        outFile.deleteOnExit();

        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(outFile)
                    .withTemplate(new ByteArrayInputStream(templateBytes))
                    .build();
            snapshotMemory("3-ExcelWriter 构建完成", memoryReport);

            ExcelPrintUtils.registerPatchExportListConverters(excelWriter);
            WriteSheet writeSheet = EasyExcel.writerSheet().build();

            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            excelWriter.fill(data(), fillConfig, writeSheet);
            snapshotMemory("4-列表 fill 完成(含 forceNewRow)", memoryReport);

//            List<List<String>> totalListList = new ArrayList<>();
//            List<String> totalList = new ArrayList<>();
//            totalListList.add(totalList);
//            totalList.add(null);
//            totalList.add(null);
//            totalList.add(null);
//            totalList.add("统计:1000");
//
//            WriteTable writeTable = EasyExcel.writerTable(1)
//                    .relativeHeadRowIndex(0)
//                    .needHead(false)
//                    .build();
//            excelWriter.write(totalListList, writeSheet, writeTable);
//            snapshotMemory("5-统计行 write(Table)完成", memoryReport);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
            snapshotMemory("6-finish 后", memoryReport);
        }

        log.info("输出文件: {}", outFile.getAbsolutePath());
        log.info("各阶段内存快照:\n{}", String.join("\n", memoryReport));
    }

    private static void snapshotMemory(String label, List<String> sink) {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long max = rt.maxMemory();
        String line = String.format(
                "%s | used=%d MiB | committed(total)=%d MiB | max=%d MiB | free=%d MiB",
                label,
                used >> 20,
                total >> 20,
                max >> 20,
                free >> 20);
        sink.add(line);
        log.info("[memory] {}", line);
    }

    private List<DictCityDTO.PagingViewDTO> data() {
        List<DictCityDTO.PagingViewDTO> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            DictCityDTO.PagingViewDTO dto = new DictCityDTO.PagingViewDTO();
            dto.setId(Integer.toString(i));
            dto.setName("城市" + i);
            dto.setKingdeeCode("11222");
            dto.setUpdateTime(LocalDateTime.now());
            dto.setUpdateUserName("system");
            dto.setParentName("");
            list.add(dto);
        }
        return list;
    }
}
