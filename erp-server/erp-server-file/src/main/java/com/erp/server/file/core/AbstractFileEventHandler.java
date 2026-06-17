package com.erp.server.file.core;

import com.alibaba.excel.write.handler.WriteHandler;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractFileEventHandler<T> implements FileEventHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public abstract void handle(FileTask fileTask);

    protected String buildDownloadFileName(FileTask fileTask) {
        return buildDownloadFileName(fileTask, getExcelPath());
    }

    protected String buildDownloadFileName(FileTask fileTask, String excelPath) {
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        return date + name + excelPath.substring(excelPath.lastIndexOf("."));
    }

    @Deprecated
    protected abstract List<T> getData(FileTask fileTask);

    protected abstract String getExcelPath();

    public <R> R readValue(String params, TypeReference<R> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            // 与 AbstractDynamicHeadersFileEventHandler.readValue 对齐：保留异常链便于排障
            ServiceException ex = new ServiceException("导出参数解析失败：" + e.getOriginalMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * 按完整 Jackson 类型解析 JSON（用于泛型参数等场景）
     */
    protected <R> R readValue(String params, JavaType javaType) {
        try {
            return objectMapper.readValue(params, javaType);
        } catch (JsonProcessingException e) {
            ServiceException ex = new ServiceException("导出参数解析失败：" + e.getOriginalMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
