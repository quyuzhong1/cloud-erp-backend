package com.sdk.wx.miniapp.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author jack
 * @date 2024-04-03
 */
@Data
public class WxSubscribeResponse implements Serializable {

    private String errmsg;

    private Integer errcode;
}
