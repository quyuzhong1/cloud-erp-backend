package com.sdk.oms.temu.dto;

import lombok.Data;

@Data
public class TemuEntity {

    private String type;
    private Integer timestamp;
    private String app_key;
    private String data_type;
    private String access_token;
    private String sendType;
    private Object sendRequestList;
    private String sign;

}
