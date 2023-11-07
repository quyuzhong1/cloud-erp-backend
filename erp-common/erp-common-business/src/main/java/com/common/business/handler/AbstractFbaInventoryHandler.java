package com.common.business.handler;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.UniqueDto;

import java.util.List;

/**
 * FBA仓库处理器抽象类
 *
 * @author Jim
 * @since 2023-11-03
 */
public abstract class AbstractFbaInventoryHandler<T extends CleanBaseDTO, R extends UniqueDto> implements IBusinessHandler<T, R> {
    public PlatformDataDTO<T, R> pullHandle(JobTaskDTO data) {
        // 调用数据下载功能
        List<T> sourceDataList = download(data);
        List<R> targetDataList = convert(sourceDataList);
        return new PlatformDataDTO<>(sourceDataList, targetDataList);
    }

    /**
     * 平台数据下载数据
     */
    public abstract List<T> download(JobTaskDTO data);

    /**
     * 平台数据转换为mq数据
     */
    public abstract List<R> convert(List<T> sourceDataList);

    /**
     * 是否发送MQ
     * true=发送
     * false=不发送（有其他详情需要额外拉取）
     */
    public Boolean getIsSendMq() {
        return Boolean.TRUE;
    }

    /**
     * 下载详情数据
     */
    public T downloadDetail(T dto, JSONObject extendObj) {
        return dto;
    }

}