package com.erp.server.msg.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: MailConfigParam
 * @Description: TODO
 * @CreateTime: 2023-04-21  15:05
 * @Author: zhangchunlin
 */
@Data
public class MailConfigParam implements Serializable {

    private String host;

    private String nickname;

    private String username;

    private String password;

    private Boolean useSSl;

    private Integer sslPort;

    private Boolean isDefault;

}