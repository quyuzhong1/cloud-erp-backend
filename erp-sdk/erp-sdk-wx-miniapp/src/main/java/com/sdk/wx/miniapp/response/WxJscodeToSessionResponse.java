package com.sdk.wx.miniapp.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author jack
 * @date 2024-04-03
 */
@Data
public class WxJscodeToSessionResponse implements Serializable {

    private String sessionKey;

    private String unionid;

    private String errmsg;

    private String openid;

    private Integer errcode;
}
