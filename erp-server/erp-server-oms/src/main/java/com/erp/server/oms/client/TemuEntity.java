package com.erp.server.oms.client;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
