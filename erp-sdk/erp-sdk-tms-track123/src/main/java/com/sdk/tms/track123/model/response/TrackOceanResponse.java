package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 海运响应参数
 * @author Will
 * @date: 2024/4/9 15:54
 */
@Data
public class TrackOceanResponse implements Serializable {
    private String code;
    private OceanResponseData data;
    private String msg;
}
