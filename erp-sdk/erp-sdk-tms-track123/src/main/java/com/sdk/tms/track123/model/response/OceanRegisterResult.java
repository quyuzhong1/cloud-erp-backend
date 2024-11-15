package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName RegisterResult

 * @date 2023年11月21日
 * @version: 1.0
 */
@Data
public class OceanRegisterResult implements Serializable {
    private String code;
    private OceanResponseData data;
    private String msg;
}
