package com.erp.server.file.handler;

import com.common.business.annotation.FileServiceType;
import com.common.core.exception.ServiceException;
import com.erp.server.file.service.FileService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class FileRegistry {

    /**
     * Excel2007 单 sheet 物理行上限（含表头），{@code sheetMaxRows} / {@code singleSheetMaxRows} 配置不得超过该值。
     */
    private static final int SHEET_MAX_ROWS_UPPER = 1_048_576;

    /**
     * 单 sheet 分组/动态表头导出默认行数上限（含表头），与 {@code file.storage.singleSheetMaxRows} 缺省值一致。
     */
    private static final int SINGLE_SHEET_MAX_ROWS_DEFAULT = 200_000;

    /**
     * 分页每页条数上限，防止运维误配极大值导致单次 Feign/内存峰值过大或超时。
     */
    private static final int MAX_PAGE_SIZE_UPPER = 20000;

    /**
     * 动态表头导出每页条数上限，含义同 {@link #MAX_PAGE_SIZE_UPPER}。
     */
    private static final int DYNAMIC_EXPORT_PAGE_SIZE_UPPER = 20000;

    /**
     * 列表数据区最多占用的物理 sheet 数上限。配合 {@code maxTemplateExpandBytes} 足迹上界，
     * 防止误配极大值放大「模板字节 × sheet 数」的 POI 展开内存峰值。
     */
    private static final int MAX_SHEET_NUM_UPPER = 200;

    /**
     * 模板展开足迹上界的硬顶（2GB）。该值本身是 OOM 安全阀、允许运维按机器内存调高，
     * 故仅设很宽松的硬顶以挡住 {@code Long.MAX_VALUE} 等荒谬误配（否则保护形同虚设），不限制正常调优区间。
     */
    private static final long MAX_TEMPLATE_EXPAND_BYTES_UPPER = 2L * 1024 * 1024 * 1024;

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
     * 单 sheet 分组报表导出时，单 sheet 最大行数（含表头），默认 200000，配置上限 {@link #SHEET_MAX_ROWS_UPPER}（1048576）。
     * 供 {@link com.erp.server.file.core.AbstractSingleSheetGroupPageFileEventHandler} 使用；
     * 多 sheet 分组报表见 {@link com.erp.server.file.core.AbstractMultiSheetGroupPageFileEventHandler}（沿用 {@link #sheetMaxRows}）。
     * <p>
     * 该配置项及对应单 sheet 分组能力尚未上线生产，默认值可按部署环境通过 {@code file.storage.singleSheetMaxRows} 调整，
     * 与多 sheet 场景的 {@link #sheetMaxRows} 语义不同，勿合并为同一配置项。
     */
    @Getter
    private static Integer singleSheetMaxRows;

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
        FileRegistry.sheetMaxRows = clampUpper("file.storage.sheetMaxRows", sheetMaxRows, SHEET_MAX_ROWS_UPPER);
    }

    @Value("${file.storage.maxSheetNum:50}")
    public void setMaxSheetNum(Integer maxSheetNum){
        FileRegistry.maxSheetNum = clampUpper("file.storage.maxSheetNum", maxSheetNum, MAX_SHEET_NUM_UPPER);
    }

    @Value("${file.storage.maxPageSize:5000}")
    public void setMaxPageSize(Integer maxPageSize){
        FileRegistry.maxPageSize = clampUpper("file.storage.maxPageSize", maxPageSize, MAX_PAGE_SIZE_UPPER);
    }

    @Value("${file.storage.maxTemplateExpandBytes:314572800}")
    public void setMaxTemplateExpandBytes(Long maxTemplateExpandBytes){
        FileRegistry.maxTemplateExpandBytes = clampUpperLong("file.storage.maxTemplateExpandBytes",
                maxTemplateExpandBytes, MAX_TEMPLATE_EXPAND_BYTES_UPPER);
    }

    @Value("${file.storage.singleSheetMaxRows:200000}")
    public void setSingleSheetMaxRows(Integer singleSheetMaxRows) {
        FileRegistry.singleSheetMaxRows = clampUpper("file.storage.singleSheetMaxRows", singleSheetMaxRows,
                SHEET_MAX_ROWS_UPPER);
    }

    @Value("${file.storage.dynamicExportPageSize:1000}")
    public void setDynamicExportPageSize(Integer dynamicExportPageSize){
        FileRegistry.dynamicExportPageSize = clampUpper("file.storage.dynamicExportPageSize", dynamicExportPageSize,
                DYNAMIC_EXPORT_PAGE_SIZE_UPPER);
    }

    /**
     * 配置上界保护：值超过业务/Excel 规格上限时 clamp 到上限并打 warn（启动时执行一次），
     * 避免运维误配极大值引发单次导出内存峰值过高或超时；下界（&lt;1）回退仍由各 {@code *OrDefault()} 处理。
     * {@code value} 为 null 时直接透传，由 {@code *OrDefault()} 兜底默认值。
     */
    private static Integer clampUpper(String key, Integer value, int upper) {
        if (value != null && value > upper) {
            log.warn("配置 {}={} 超过上限 {}，已 clamp 到上限以保护内存与超时", key, value, upper);
            return upper;
        }
        return value;
    }

    /**
     * {@code long} 版上界保护，语义同 {@link #clampUpper(String, Integer, int)}，用于 {@code maxTemplateExpandBytes} 等 long 配置。
     */
    private static Long clampUpperLong(String key, Long value, long upper) {
        if (value != null && value > upper) {
            log.warn("配置 {}={} 超过上限 {}，已 clamp 到上限以保护内存与超时", key, value, upper);
            return upper;
        }
        return value;
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
     * 单 sheet 分组报表导出时的 sheet 行数上限（含表头），未注入或非法（&lt;1）时回退 {@link #SINGLE_SHEET_MAX_ROWS_DEFAULT}（200000）。
     */
    public static int singleSheetMaxRowsOrDefault() {
        Integer configured = singleSheetMaxRows;
        return configured == null || configured < 1 ? SINGLE_SHEET_MAX_ROWS_DEFAULT : configured;
    }

    /**
     * 单份 classpath 模板原始字节上界：{@code maxTemplateExpandBytes / maxSheetNum}。
     * 最坏情况按 {@link #maxSheetNumOrDefault()} 张同结构 sheet 展开时，足迹不超过 {@link #maxTemplateExpandBytesOrDefault()}。
     */
    public static long maxSingleTemplateBytesOrDefault() {
        int maxSheets = maxSheetNumOrDefault();
        return Math.max(1L, maxTemplateExpandBytesOrDefault() / maxSheets);
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
