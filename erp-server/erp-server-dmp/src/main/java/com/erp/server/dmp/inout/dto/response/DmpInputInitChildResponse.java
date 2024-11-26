package com.erp.server.dmp.inout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子任务响应参数
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpInputInitChildResponse<T> {

    /**
     * 执行后续handler
     */
    private boolean doNextChain = true;


    /**
     * 响应数据
     */
    private T data;
}
