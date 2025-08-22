package com.sdk.oms.dht.dto;

import cn.hutool.core.text.CharSequenceUtil;
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

    private String errorDescription;

    private String traceId;

    public static  BaseResult error(String formatedErrMsg,Object... params) {
        BaseResult baseResult = new BaseResult();
        baseResult.setErrorCode(100);
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        baseResult.setErrorMessage(msg);
        return baseResult;
    }
}
