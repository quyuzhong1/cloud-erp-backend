package com.sdk.oms.temu.dto;

import jnr.ffi.annotations.In;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemuResp<T> {

    private Boolean success;

    private String requestId;

    private Integer errorCode;

    private String errorMsg;

    private T result;
}
