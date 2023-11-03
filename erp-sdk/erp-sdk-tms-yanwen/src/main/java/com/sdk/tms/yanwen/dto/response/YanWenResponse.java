package com.sdk.tms.yanwen.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
public class YanWenResponse<T> implements Serializable {
    private Boolean success;
    private String code;
    private String message;
    private T data;

    public static YanWenResponse error(String code,String error) {
        YanWenResponse apiResult = new YanWenResponse();
        apiResult.setSuccess(Boolean.FALSE);
        apiResult.setCode(code);
        apiResult.setMessage(error);
        return apiResult;
    }
}
