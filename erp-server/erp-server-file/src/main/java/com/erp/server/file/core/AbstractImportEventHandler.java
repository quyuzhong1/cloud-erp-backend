package com.erp.server.file.core;

import com.common.business.dto.base.BaseDTO;
import com.common.core.exception.ServiceException;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractImportEventHandler<T> implements FileEventHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(FileTask fileTask) {
        getData(fileTask);
    }

    /**
     * 获取数据
     *
     * @param fileTask 入参
     */
    protected abstract void getData(FileTask fileTask);

    /**
     * 获取excel路径
     */
    protected abstract String getExcelPath();

    public <P> P readValue(String params, TypeReference<P> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
