package com.erp.server.file.core;

import com.alibaba.excel.write.handler.WriteHandler;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractFileEventHandler<T> implements FileEventHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(FileTask fileTask) {
        List<T> list = getData(fileTask);
        fileTask.setCount(list.size());
        String displayName = buildDownloadFileName(fileTask);
        Path tempPath = null;
        try {
            tempPath = ExportTempFilesHandler.createTempPath(FileRegistry.getStorageTmpdir(), ".xlsx", fileTask.getUniqueWithFileName());
            new ExcelPrintUtils().patchExportListToFile(tempPath.toFile(), list, getExcelPath(),
                    getWriteHandler().toArray(new WriteHandler[0]));
            String url = FastDFSClientUtil.uploadFile(tempPath.toFile(), displayName, null);
            fileTask.setFileUrl(url);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            ExportTempFilesHandler.deleteQuietly(tempPath);
        }
    }

    protected String buildDownloadFileName(FileTask fileTask) {
        return buildDownloadFileName(fileTask, getExcelPath());
    }

    protected String buildDownloadFileName(FileTask fileTask, String excelPath) {
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        return date + name + excelPath.substring(excelPath.lastIndexOf("."));
    }

    protected abstract List<T> getData(FileTask fileTask);

    protected abstract String getExcelPath();

    public <R> R readValue(String params, TypeReference<R> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 按完整 Jackson 类型解析 JSON（用于泛型参数等场景）
     */
    protected <R> R readValue(String params, JavaType javaType) {
        try {
            return objectMapper.readValue(params, javaType);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
