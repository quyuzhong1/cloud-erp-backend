package com.sdk.oms.dht.dto;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DhtBaseResp<T> {

    /**
     * 错误码：0为成功，其他为失败
     */
    private Integer errorCode;

    private T data;

    private String errorDescription;
    private String errorMessage;
    private String traceId;


    public static <T> DhtBaseResp<T> error(String msg) {
        DhtBaseResp<T> apiResult = new DhtBaseResp<>();
        apiResult.setErrorCode(-1);
        apiResult.setErrorMessage(msg);
        return apiResult;
    }

    public static <T> DhtBaseResp<T> error(String formatedErrMsg, Object... params) {
        DhtBaseResp<T> apiResult = new DhtBaseResp<>();
        apiResult.setErrorCode(-1);
        String msg = CharSequenceUtil.format(formatedErrMsg,params);
        apiResult.setErrorMessage(msg);
        return apiResult;
    }
}
