package com.sdk.oms.temu.dto;

import com.common.core.controller.vo.ApiResult;
import jnr.ffi.annotations.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemuResp<T> {

    private Boolean success;

    private String requestId;

    private Integer errorCode;

    private String errorMsg;

    private T result;

    public static <T> TemuResp<T> error(String msg) {
        TemuResp<T> apiResult = new TemuResp<>();
        apiResult.setSuccess(false);
        apiResult.setErrorMsg(msg);
        return apiResult;
    }
}
