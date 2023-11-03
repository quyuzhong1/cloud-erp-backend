package com.common.business.handler;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;

/**
 * 业务处理器
 * @author Cloud
 * @param <T>
 */
public interface IBusinessHandler<T extends CleanBaseDTO,R extends UniqueDto> {
    /**
     * 拉取数据处理
     * @param data
     */
    PlatformDataDTO<T, R> pullHandle(JobTaskDTO data);

    /**
     * 获取目标平台
     * @return
     */
    String getTargetPlatform();

    /**
     * 是否发送MQ
     */
    Boolean getIsSendMq();

    /**
     * 下载详情数据
     */
    T downloadDetail(T dto, JSONObject extendObj);

}