package com.sdk.oms.wildberries.dto;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zdy
 * @ClassName BaseResponse
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
public class BaseResponse {
    private String title;
    private String detail;
    private String code;
    private String requestId;
    private String origin;
    private String timestamp;
    /**
     * 状态 OK
     */
    private String status;
    private String statusText;

    public Boolean isSuccess(){
        return CharSequenceUtil.isBlank(status) || "OK".equals(status);
    }

    public String getMsg(){
        return statusText;
    }
}
