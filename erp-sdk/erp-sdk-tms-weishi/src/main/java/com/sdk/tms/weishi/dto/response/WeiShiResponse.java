package com.sdk.tms.weishi.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class WeiShiResponse<T> implements Serializable {

    //响应标志，Success表示成功，Failure表示失败
    private String ask;

    //消息提示
    private String message;

    //消息提示
    private String messageEng;

    private Error error;

    private List<Result> result;

    private T data;

    public static WeiShiResponse<String> error(String code, String error) {
        WeiShiResponse<String> apiResult = new WeiShiResponse<>();
        apiResult.setAsk("Failure");
        apiResult.setMessage(error);
        return apiResult;
    }

    @Data
    public static class Error implements Serializable{

        private String errMessage;

        private String errCode;
    }

    @Data
    public static class Result implements Serializable{

        private String ask;

        private String message;
    }
}
