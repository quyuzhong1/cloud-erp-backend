package com.erp.server.file.core;

import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.common.business.dto.StatementDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.IOException;
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
        StringBuilder sb = new StringBuilder();
        String excelPath = getExcelPath();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        try {
            byte[] bytes = new ExcelPrintUtils().patchExport(detailData, mainData, excelPath);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
            fileTask.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    protected abstract StatementDTO<R, T> getData(FileTask fileTask);

    /**
     * 获取excel路径
     */
    protected abstract String getExcelPath();

    public <S> S readValue(String params, TypeReference<S> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
