package com.erp.server.msg.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: FeishuConfigParam
 * @Description: TODO
 * @CreateTime: 2023-04-21  15:05
 * @Author: zhangchunlin
 */
@Data
public class FeishuConfigParam implements Serializable {

    private String clientSecret;

    private String clientId;

    private String redirectLoginUri;

    private String redirectBindingUri;

    private String appUrl;

    private Boolean isDefault;

}