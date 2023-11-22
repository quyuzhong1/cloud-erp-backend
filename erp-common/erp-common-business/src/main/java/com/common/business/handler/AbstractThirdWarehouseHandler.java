package com.common.business.handler;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.threadlocal.ThirdWarehouseContext;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 第三方仓处理器抽象类
 */
public abstract class AbstractThirdWarehouseHandler<T extends CleanBaseDTO, R extends UniqueDto> implements IBusinessHandler<T, R> {

    protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PlatformDataDTO<T, R> pullHandle(JobTaskDTO data) {
        try {
            //设置thread-local
            Map<String, Object> authMap = data.getApiParam().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
            ThirdWarehouseContext.setAuthMap(authMap);
            // 调用数据下载功能
            List<T> sourceDataList = download(data);
            List<R> targetDataList = convert(sourceDataList);
            return new PlatformDataDTO<>(sourceDataList, targetDataList);
        }finally {
            ThirdWarehouseContext.remove();
        }
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