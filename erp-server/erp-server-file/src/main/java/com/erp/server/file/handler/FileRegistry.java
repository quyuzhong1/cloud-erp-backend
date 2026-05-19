package com.erp.server.file.handler;

import com.common.business.annotation.FileServiceType;
import com.erp.server.file.exception.BusinessException;
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
     * 列表数据区最多占用的物理 sheet 数（含 sheet0）。超出则抛 {@link BusinessException}，避免无限克隆。
     * 默认全局指定：50
     */
    @Getter
    private static Integer maxSheetNum;

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

    @Value("${file.storage.maxSheetNow:50}")
    public void setMaxSheetNum(Integer maxSheetNow){
        FileRegistry.maxSheetNum = maxSheetNow;
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
