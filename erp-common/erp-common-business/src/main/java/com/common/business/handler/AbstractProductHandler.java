package com.common.business.handler;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.UniqueDto;

import java.util.List;

/**
 * 产品处理器抽象类
 * @author Cloud
 */
public abstract class AbstractProductHandler<T extends CleanBaseDTO,R extends UniqueDto> implements IBusinessHandler<T,R> {
    public PlatformDataDTO<T, R> pullHandle(JobTaskDTO data) {
        // 调用数据下载功能
        List<T> sourceDataList = download(data);
        List<R> targetDataList = convert(sourceDataList);
        return new PlatformDataDTO<>(sourceDataList, targetDataList);
    }


    public List<?> pushHandle(JobTaskDTO data) {
        // 组装数据
        List<?> resultList = pushDataPackage(data);
        // 1. Deserialize the message body to PlatformOrderDataDTO
        return resultList;
    }

    /**
     * 平台数据下载数据
     * @return
     */
    abstract List<T> download(JobTaskDTO data);

    /**
     * 平台数据转换为mq数据
     * @param sourceDataList
     * @return
     */
    abstract List<R> convert(List<T> sourceDataList);

    /**
     * 推送数据封装
     * @param data
     * @return
     */
    public abstract List<?> pushDataPackage(JobTaskDTO data);
}