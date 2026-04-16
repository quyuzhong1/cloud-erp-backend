package com.sdk.wx.miniapp.api.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import com.sdk.wx.miniapp.constants.WxConstants;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import com.sdk.wx.miniapp.response.WxTokenResponse;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Component
public class BaseWxMiniAppServiceImpl implements WxMiniAppService {

  @Value("${wx.appId}")
  private String appId;
  @Value("${wx.appsecret}")
  private String appsecret;
  public static final String grantType = "authorization_code";

  @Resource
  private RedisUtil redisUtil;


  @Override
  public  String getAccessToken() {
    String accessToken = "";
    // Redis缓存实现
    String key = RedisCacheConstants.WECHAT_ACCESS_TOKEN_KEY;
    if (redisUtil.hasKey(key)) {
      accessToken = String.valueOf(redisUtil.get(key));
    } else {
      String url = String.format(WxConstants.BASE_URL + WxConstants.GET_ACCESS_TOKEN, appId, appsecret);
      String bodyStr = sendGet(url);
      WxTokenResponse resultMap = JSONUtil.toBean(bodyStr, WxTokenResponse.class);
      // 调用微信接口并缓存结果
      redisUtil.set(key, resultMap.getAccessToken(), 7000);
      accessToken = resultMap.getAccessToken();
    }
    return accessToken;
  }


  @Override
  public  WxJscodeToSessionResponse jsCode2SessionInfo(String jsCode) {
    if(StringUtil.isBlank(jsCode)){
        return null;
    }

    String url = String.format(WxConstants.BASE_URL + WxConstants.JSCODE_TO_SESSION, appId, appsecret,jsCode);
    String bodyStr = sendGet(url );
    return JSONUtil.toBean(bodyStr, WxJscodeToSessionResponse.class);

  }


  @Override
  public  String sendSubscribeMsg(String jsonStr) {
    //获取access_token
    String accessToken = getAccessToken();
    if (accessToken == null || accessToken.isEmpty()) {
      log.error("Access token is null or empty.");
      throw new RuntimeException("Failed to obtain access token.");
    }
    String url = String.format(WxConstants.BASE_URL + WxConstants.SEND_SUBSCRIBE_MESSAGE,accessToken);
    Map<String, String> headers = new HashMap();
    headers.put("Content-Type", "application/json");
    headers.put("Accept", "application/json");
    log.info("baseUrl：{}", url);
    String bodyStr = OkHttpUtils.doPostJson(url, jsonStr, headers);
    // 验证返回值
    if (bodyStr == null || bodyStr.isEmpty()) {
      log.error("Response body is null or empty.");
      throw new RuntimeException("Empty response received from server.");
    }
    log.info("bodyStr：{}", bodyStr);
    return  bodyStr;
  }

  private static String sendGet(String url){
    log.info("baseUrl：{}", url);

    String bodyStr = "";
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");
    headers.put("Connection", "keep-alive");
    try {
      bodyStr = OkHttpUtils.doGet(url, new HashMap<String, Object>(), headers);
      log.info("bodyStr：{}", bodyStr);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return bodyStr;
  }

}
