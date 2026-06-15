package com.erp.server.file.handler;

import com.common.business.annotation.FileServiceType;
import com.common.core.exception.ServiceException;
import com.erp.server.file.service.FileService;
import lombok.Getter;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 业务初始化处理
 *
 * @author Cloud
 */
@Service
public class FileRegistry {
    private final Map<String, FileService> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

    @Value("${file.storage.type}")
    private String storageType;

    /**
     * 临时 xlsx 写入目录；未配置时使用 {@code java.io.tmpdir}
     */
    @Getter
    private static String storageTmpdir;

    /**
     * Excel2007 单 sheet 最大行数（含表头） 1_048_576
     * 默认全局指定：100000
     */
    @Getter
    private static Integer sheetMaxRows;

    /**
     * 列表数据区最多占用的物理 sheet 数（含 sheet0）。超出则抛 {@link ServiceException}，避免无限克隆。
     * 默认全局指定：50
     */
    @Getter
    private static Integer maxSheetNum;

    /**
     * 文件每次分页处理每页最大数量
     */
    @Getter
    private static Integer maxPageSize;

    /**
     * 模板展开足迹（模板字节 × 数据 sheet 数）硬上界，单位字节。
     * 用于在 POI 整本克隆多 sheet 前对峰值内存做固定上界保护，超出则早失败抛 {@link ServiceException}，避免 OOM。
     * 默认全局指定：300MB（314572800）。
     */
    @Getter
    private static Long maxTemplateExpandBytes;

    /**
     * 动态表头导出每页条数。动态表头单行 DynamicExcelDTO 体积通常大于固定模板行对象，
     * 沿用更保守的批次（默认 1000）控制 Feign/内存峰值与超时，避免大宽表导出回归。
     */
    @Getter
    private static Integer dynamicExportPageSize;

    /**
     * 冒号后无内容表示「缺省属性时用空字符串」；业务侧应对空白串再回退到 {@code java.io.tmpdir}（见 ExportTempFilesHandler 等）。
     * 若需缺省为 null，可改为 {@code ${file.storage.tmpdir:#{null}}}（SpEL）。
     */
    @Value("${file.storage.tmpdir:}")
    public void setStorageTmpdir(String storageTmpdir) {
        FileRegistry.storageTmpdir = storageTmpdir;
    }

    @Value("${file.storage.sheetMaxRows:100000}")
    public void setSheetMaxRows(Integer sheetMaxRows){
        FileRegistry.sheetMaxRows = sheetMaxRows;
    }

    @Value("${file.storage.maxSheetNum:50}")
    public void setMaxSheetNum(Integer maxSheetNum){
        FileRegistry.maxSheetNum = maxSheetNum;
    }

    @Value("${file.storage.maxPageSize:5000}")
    public void setMaxPageSize(Integer maxPageSize){
        FileRegistry.maxPageSize = maxPageSize;
    }

    @Value("${file.storage.maxTemplateExpandBytes:314572800}")
    public void setMaxTemplateExpandBytes(Long maxTemplateExpandBytes){
        FileRegistry.maxTemplateExpandBytes = maxTemplateExpandBytes;
    }

    @Value("${file.storage.dynamicExportPageSize:1000}")
    public void setDynamicExportPageSize(Integer dynamicExportPageSize){
        FileRegistry.dynamicExportPageSize = dynamicExportPageSize;
    }

    /**
     * 导出分页每页条数：读取 {@code file.storage.maxPageSize}，未注入或非法（&lt;1）时回退 5000，
     * 与 {@link #setMaxPageSize} 的缺省配置一致。
     */
    public static int exportPageSize() {
        Integer configured = maxPageSize;
        return configured == null || configured < 1 ? 5000 : configured;
    }

    /**
     * 单 sheet 最大数据行数（含表头预留），未注入或非法（&lt;1）时回退 100000。
     */
    public static int sheetMaxRowsOrDefault() {
        Integer configured = sheetMaxRows;
        return configured == null || configured < 1 ? 100000 : configured;
    }

    /**
     * 列表数据区最多占用的物理 sheet 数，未注入或非法（&lt;1）时回退 50。
     */
    public static int maxSheetNumOrDefault() {
        Integer configured = maxSheetNum;
        return configured == null || configured < 1 ? 50 : configured;
    }

    /**
     * 模板展开足迹硬上界（字节），未注入或非法（&lt;1）时回退 300MB（314572800）。
     */
    public static long maxTemplateExpandBytesOrDefault() {
        Long configured = maxTemplateExpandBytes;
        return configured == null || configured < 1 ? 314572800L : configured;
    }

    /**
     * 动态表头导出每页条数：读取 {@code file.storage.dynamicExportPageSize}，未注入或非法（&lt;1）时回退 1000，
     * 与历史固定批次一致，避免大宽表动态表头导出因批次过大引发内存/超时回归。
     */
    public static int dynamicExportPageSize() {
        Integer configured = dynamicExportPageSize;
        return configured == null || configured < 1 ? 1000 : configured;
    }

    @PostConstruct
    public void init() {
        Map<String, FileService> beans = context.getBeansOfType(FileService.class);
        for (Object bean : beans.values()) {
            Class<?> actualClass = AopProxyUtils.ultimateTargetClass(bean);
            FileServiceType platformAnnotation = actualClass.getAnnotation(FileServiceType.class);
            handlers.put(platformAnnotation.value().getCode(), (FileService) bean);
        }
    }

    public FileService getHandler(String platform) {
        return handlers.get(platform);
    }
    public FileService getHandler() {
        FileService fileService = handlers.get(storageType);
        if (Objects.isNull(fileService)){
            throw new RuntimeException("文件存储类型不存在");
        }else {
            return fileService;
        }
    }
}
