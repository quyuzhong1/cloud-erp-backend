package com.common.business.handler;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.UniqueDto;

import java.util.List;

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
     * 拉取数据处理
     * @param data
     */
    List<?> pushHandle(JobTaskDTO data);
}