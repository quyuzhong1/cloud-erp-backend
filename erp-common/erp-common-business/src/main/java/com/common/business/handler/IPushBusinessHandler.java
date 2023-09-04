package com.common.business.handler;

import com.common.business.dto.JobTaskDTO;

import java.util.List;

/**
 * 推送业务处理器
 * @author Cloud
 * @param <T>
 */
public interface IPushBusinessHandler<T> {
    /**
     * 拉取数据处理
     * @param data
     */
    List<T> handle(JobTaskDTO data);
}