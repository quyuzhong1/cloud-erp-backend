package com.common.business.handler;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.UniqueDto;

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
}