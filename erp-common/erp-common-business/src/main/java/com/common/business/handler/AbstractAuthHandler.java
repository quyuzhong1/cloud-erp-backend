package com.common.business.handler;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.UniqueDto;

/**
 * 授权处理器抽象类
 * @author Cloud
 * @param <T>
 */
public abstract class AbstractAuthHandler<T extends CleanBaseDTO,R extends UniqueDto> implements IBusinessHandler<T,R> {
    // Common logic for refund handling can be defined here

    /**
     * 获取授权
     * @return
     */
    public abstract T auth();
}