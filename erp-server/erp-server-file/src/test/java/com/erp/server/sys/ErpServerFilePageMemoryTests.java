package com.erp.server.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.handler.FileRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 分页 OFFSET 写入内存与多 sheet 切换（{@link AbstractPageFileEventHandler} 私有方法反射调用）。
 */
@Slf4j
public class ErpServerFilePageMemoryTests {

    private static final String TEMPLATE_RESOURCE = "excel/test/offsetPagingMultiSheetTemplate.xlsx";
    private static final int RESERVED_HEADER_ROWS = 20;
    /** 与 {@link FileRegistry#setSheetMaxRows} 默认预留行配合，使单 sheet 数据上限为 10000 行 */
    private static final int SHEET_MAX_ROWS_FOR_TEST = 10_000 + RESERVED_HEADER_ROWS;
    private static final int TOTAL_EXPORT_ROWS = 25_000;
    private static final int PAGE_SIZE = 5_000;

    private static Integer savedSheetMaxRows;

    @BeforeClass
    public static void initTemplateAndSheetConfig() throws Exception {
        Field f = FileRegistry.class.getDeclaredField("sheetMaxRows");
        f.setAccessible(true);
        savedSheetMaxRows = (Integer) f.get(null);
        f.set(null, SHEET_MAX_ROWS_FOR_TEST);

        URL root = ErpServerFilePageMemoryTests.class.getResource("/");
        assertNotNull(root);
        Path dir = Paths.get(root.toURI()).resolve("excel/test");
        Files.createDirectories(dir);
        Path out = dir.resolve("offsetPagingMultiSheetTemplate.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            for (int i = 0; i < 6; i++) {
                XSSFSheet s = wb.createSheet("d" + i);
                XSSFRow r = s.createRow(0);
                r.createCell(0).setCellValue("{.id}");
            }
            try (OutputStream os = Files.newOutputStream(out)) {
                wb.write(os);
            }
        }
    }

    @AfterClass
    public static void restoreSheetConfig() throws Exception {
        Field f = FileRegistry.class.getDeclaredField("sheetMaxRows");
        f.setAccessible(true);
        f.set(null, savedSheetMaxRows);
    }

    @Test
    public void testWriteOffsetBatches() throws Exception {
        Runtime rt = Runtime.getRuntime();
        consumeMemoryBaseline(rt);

        long memBefore = usedHeapBytes(rt);
        log.info("[memory] before writeOffsetBatches usedHeap={} MB", memBefore / 1024 / 1024);

        AtomicLong peakDuring = new AtomicLong(memBefore);
        ScheduledExecutorService sampler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heap-sampler");
            t.setDaemon(true);
            return t;
        });
        sampler.scheduleAtFixedRate(() -> {
            long u = usedHeapBytes(rt);
            peakDuring.updateAndGet(cur -> Math.max(cur, u));
        }, 0, 50, TimeUnit.MILLISECONDS);

        File out = Files.createTempFile("offset-batch-mem-", ".xlsx").toFile();
        try {
            OffsetMemoryTestHandler handler = new OffsetMemoryTestHandler();
            Method m = AbstractPageFileEventHandler.class.getDeclaredMethod("writeOffsetBatches", File.class, Object.class);
            m.setAccessible(true);
            int written = (Integer) m.invoke(handler, out, "");
            assertEquals(TOTAL_EXPORT_ROWS, written);

            long memAfterGc = usedHeapBytesAfterGc(rt);
            sampler.shutdownNow();
            boolean stopped = sampler.awaitTermination(2, TimeUnit.SECONDS);
            if (!stopped) {
                log.warn("heap sampler did not terminate cleanly");
            }

            log.info("[memory] peakDuringPaging usedHeap={} MB (sampled every 50ms)",
                    peakDuring.get() / 1024 / 1024);
            log.info("[memory] after writeOffsetBatches + gc usedHeap={} MB", memAfterGc / 1024 / 1024);

            try (XSSFWorkbook wb = new XSSFWorkbook(out)) {
                assertTrue("应至少使用 3 个 sheet（25000 行 / 每 sheet 10000 行）", wb.getNumberOfSheets() >= 3);
                XSSFSheet s2 = wb.getSheetAt(2);
                assertTrue("第 3 个 sheet 应有数据行", s2.getLastRowNum() >= 1);
            }
        } finally {
            //noinspection ResultOfMethodCallIgnored
            out.delete();
        }
    }

    private static void consumeMemoryBaseline(Runtime rt) {
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static long usedHeapBytes(Runtime rt) {
        return rt.totalMemory() - rt.freeMemory();
    }

    private static long usedHeapBytesAfterGc(Runtime rt) {
        consumeMemoryBaseline(rt);
        return usedHeapBytes(rt);
    }

    private static final class OffsetMemoryTestHandler extends AbstractPageFileEventHandler<MemoryTestRow, String> {

        @Override
        protected PagingVO<MemoryTestRow> getPageData(PagingDTO<String> dto) {
            int curr = dto.getCurrPage();
            int size = dto.getPageSize();
            int start = (curr - 1) * size;
            if (start >= TOTAL_EXPORT_ROWS) {
                return new PagingVO<>(Collections.emptyList(), TOTAL_EXPORT_ROWS, size, curr);
            }
            int end = Math.min(start + size, TOTAL_EXPORT_ROWS);
            List<MemoryTestRow> list = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                list.add(new MemoryTestRow(i));
            }
            return new PagingVO<>(list, TOTAL_EXPORT_ROWS, size, curr);
        }

        @Override
        protected String getExcelPath() {
            return TEMPLATE_RESOURCE;
        }

        @Override
        public FileTaskEventEnum getEvent() {
            return FileTaskEventEnum.EXPORT_SYS_GLOBAL_AREA;
        }

        @Override
        protected List<MemoryTestRow> getData(FileTask fileTask) {
            return Collections.emptyList();
        }

        @Override
        protected int getPageSize() {
            return PAGE_SIZE;
        }
    }

    public static final class MemoryTestRow {
        private final long id;

        public MemoryTestRow(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }
}
