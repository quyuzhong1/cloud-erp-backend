package com.erp.server.file.core;

import com.common.business.dto.StatementDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.erp.server.file.handler.FileRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractDetailPageFileEventHandler<R, T> implements FileEventHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(FileTask fileTask) {
        StatementDTO<R, T> detail = getData(fileTask);
        R mainData = detail.getMainData();
        List<T> detailData = detail.getDetailData();
        fileTask.setCount(detailData.size());
        String displayName = buildDownloadFileName(fileTask);
        Path tempPath = null;
        try {
            tempPath = ExportTempFilesHandler.createTempPath(FileRegistry.getStorageTmpdir(), ".xlsx", fileTask.getUniqueWithFileName());
            new ExcelPrintUtils().patchExportDetailToFile(tempPath.toFile(), detailData, mainData, getExcelPath());
            String url = FastDFSClientUtil.streamUploadFile(tempPath.toFile(), displayName, null);
            fileTask.setFileUrl(url);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            ExportTempFilesHandler.deleteQuietly(tempPath);
        }
    }

    protected String buildDownloadFileName(FileTask fileTask) {
        String excelPath = getExcelPath();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        return date + name + excelPath.substring(excelPath.lastIndexOf("."));
    }

    protected abstract StatementDTO<R, T> getData(FileTask fileTask);

    protected abstract String getExcelPath();

    public <S> S readValue(String params, TypeReference<S> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
