package com.erp.server.sys;

import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.ErpServerFileApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 在开发环境配置下真实调用 {@link FastDFSClientUtil#uploadFile(File, String, Map)}（及两参数委托）。
 * <p>
 * 依赖 Spring 容器创建 {@link FastDFSClientUtil} 以注入 {@code fdfs.configFile}（通常来自 Nacos shared-config），
 * 并需本机可访问 dev 的 Tracker/Storage。若返回 {@code null} 或上下文启动失败，请检查网络与 FastDFS 配置。
 * <p>
 * 全仓库直接三参数调用仅见于 erp-server-file 的 Abstract*FileEventHandler；其余多为
 * {@link FastDFSClientUtil#uploadFile(File, String)} → {@code meta == null}。
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ErpServerFileApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
public class ErpServerFileUploadTests {

    @Autowired
    private FastDFSClientUtil fastDFSClientUtil;

    @Before
    public void requireFastDFSBean() {
        assertNotNull("FastDFSClientUtil 未注入，请确认 dev 配置可加载 fdfs.configFile", fastDFSClientUtil);
    }

    private static String assertUploadReturnsGroup1(File file, String fileName, Map<String, String> meta) {
        String url = FastDFSClientUtil.uploadFile(file, fileName, meta);
        assertNotNull(
                "FastDFS 上传返回 null，请确认 dev 环境 fdfs、Tracker/Storage 可达（bootstrap-dev + Nacos common 等）",
                url);
        log.info("upload ok fileName={} url={}", fileName, url);
        return url;
    }

    /**
     * {@link com.erp.server.file.core.AbstractFileEventHandler}、
     * {@link com.erp.server.file.core.AbstractDetailPageFileEventHandler}、
     * {@link com.erp.server.file.core.AbstractPageFileEventHandler}：
     * 临时 xlsx + 展示文件名 + {@code meta == null}（三者调用形态相同，合并为一次实际上传）。
     */
    @Test
    public void uploadFile_likeAbstractExportHandlers() throws Exception {
        File temp = Files.createTempFile("export-", ".xlsx").toFile();
        try {
            Files.write(temp.toPath(), "excel-bytes".getBytes(StandardCharsets.UTF_8));
            assertUploadReturnsGroup1(temp, "20250101销售导出.xlsx", null);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    /**
     * {@link com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler}：
     * 动态表头导出文件名形态 + {@code meta == null}。
     */
    @Test
    public void uploadFile_likeAbstractDynamicHeadersFileEventHandler() throws Exception {
        File temp = Files.createTempFile("dynamic-headers-", ".xlsx").toFile();
        try {
            Files.write(temp.toPath(), "dynamic".getBytes(StandardCharsets.UTF_8));
            assertUploadReturnsGroup1(temp, "20250101动态列导出.xlsx", null);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    /**
     * {@link com.erp.server.file.service.impl.FastDFSDownloadImpl#uploadFile(File, String)} 及各类导入回写：
     * 两参数委托到三参数且 {@code meta == null}。
     */
    @Test
    public void uploadFile_likeTwoArgDelegate_fastDFSDownloadAndImports() throws Exception {
        File temp = Files.createTempFile("import-result-", ".xlsx").toFile();
        try {
            Files.write(temp.toPath(), "result".getBytes(StandardCharsets.UTF_8));
            String url = FastDFSClientUtil.uploadFile(temp, "采购导入错误回写.xlsx");
            assertNotNull(url);
            assertTrue(url.startsWith("group1"));
            log.info("two-arg delegate url={}", url);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    /**
     * 对齐 {@link FastDFSClientUtil#uploadFile(org.springframework.web.multipart.MultipartFile)} 落地后：
     * 小写逻辑文件名 + {@code meta == null}。
     */
    @Test
    public void uploadFile_likeAfterMultipartToFile_lowerCaseName() throws Exception {
        File temp = Files.createTempFile("multi-", ".pdf").toFile();
        try {
            Files.write(temp.toPath(), "pdf".getBytes(StandardCharsets.UTF_8));
            assertUploadReturnsGroup1(temp, "label.pdf", null);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    /** 空文件（0 字节）+ {@code meta == null}。 */
    @Test
    public void uploadFile_emptyTempFile_metaNull() throws Exception {
        File empty = Files.createTempFile("empty-", ".xlsx").toFile();
        try {
            assertUploadReturnsGroup1(empty, "20250101空.xlsx", null);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            empty.delete();
        }
    }

    /** 三参数且带元数据（仓库内 File 侧少见，覆盖协议与客户端行为）。 */
    @Test
    public void uploadFile_withMetaMap() throws Exception {
        File temp = Files.createTempFile("meta-", ".bin").toFile();
        try {
            Files.write(temp.toPath(), "x".getBytes(StandardCharsets.UTF_8));
            Map<String, String> meta = new HashMap<>();
            meta.put("source", "ErpServerFileUploadTests");
            assertUploadReturnsGroup1(temp, "data.bin", meta);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    /** 子目录下的 {@link File}（相对父目录路径），与工具类/new File 场景一致。 */
    @Test
    public void uploadFile_relativePathFile_resolvesLikeJvm() throws Exception {
        File base = Files.createTempDirectory("rel-upload").toFile();
        File child = new File(base, "child.txt");
        try {
            Files.write(child.toPath(), "rel".getBytes(StandardCharsets.UTF_8));
            assertUploadReturnsGroup1(child, "child.txt", null);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            child.delete();
            //noinspection ResultOfMethodCallIgnored
            base.delete();
        }
    }
}
