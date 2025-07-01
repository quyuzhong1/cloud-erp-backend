package com.erp.server.file.handler;

import com.common.business.annotation.FileServiceType;
import com.erp.server.file.service.FileService;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Valid;
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
