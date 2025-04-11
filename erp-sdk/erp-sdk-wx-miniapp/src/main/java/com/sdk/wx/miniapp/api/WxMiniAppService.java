package com.sdk.wx.miniapp.api;

import com.sdk.wx.miniapp.request.SubscribeMsgRequest;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;

/**
 *
 */
public interface WxMiniAppService {
  /**
   * 获取登录后的session信息
   */
  String getAccessToken();

  WxJscodeToSessionResponse jsCode2SessionInfo(String jsCode);

  String sendSubscribeMsg(SubscribeMsgRequest request);
}
