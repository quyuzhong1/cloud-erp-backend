package com.sdk.oms.dht.dto;

import com.alibaba.fastjson.annotation.JSONField;
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
public class DhtAuthDTO extends BaseResult{

    @JSONField(name = "corpId")
    private String corpId;
    @JSONField(name = "mobile")
    private String mobile;
    @JSONField(name = "openUserId")
    private String openUserId;
    @JSONField(name = "url")
    private String url;
    @JSONField(name = "expiresIn")
    private Integer expiresIn;
    @JSONField(name = "corpAccessToken")
    private String corpAccessToken;
    @JSONField(name = "name")
    private String name;
    @JSONField(name = "enterpriseId")
    private Integer enterpriseId;
    @JSONField(name = "account")
    private String account;
    @JSONField(name = "status")
    private String status;
}
