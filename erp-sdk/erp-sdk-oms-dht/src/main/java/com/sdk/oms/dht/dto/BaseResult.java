package com.sdk.oms.dht.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseResult {

    /**
     * 结果返回码（0表示成功）
     */
    protected int errorCode = 99;

    /**
     * 返回结果信息
     */
    protected String errorMessage;
}
