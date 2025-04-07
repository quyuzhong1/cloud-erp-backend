package com.sdk.wx.miniapp.api.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import com.sdk.wx.miniapp.constants.WxConstants;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import com.sdk.wx.miniapp.response.WxTokenResponse;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BaseWxMiniAppServiceImpl implements WxMiniAppService {

  public static final String APPID = "wxe61ca423cded1079";

  public static final String APPSECRET = "267176f1c8c9e1e5e5af05c81f00976d";

  public static final String GRANT_TYPE = "authorization_code";

  @Resource
  private RedisUtil redisUtil;

  public static void main(String[] args) {
    String url = String.format(WxConstants.BASE_URL + WxConstants.GET_ACCESS_TOKEN, APPID, APPSECRET);
    String bodyStr = sendGet(url, new HashMap<>());
    WxTokenResponse resultMap = JSONUtil.toBean(bodyStr, WxTokenResponse.class);
    System.out.println(resultMap.getAccessToken());
  }

  @Override
  public  String getAccessToken() {
    String accessToken = "";
    // Redis缓存实现
    String key = "wechat:access_token";
    if (redisUtil.hasKey(key)) {
      accessToken = String.valueOf(redisUtil.get(key));
    } else {
      String url = String.format(WxConstants.BASE_URL + WxConstants.GET_ACCESS_TOKEN, APPID, APPSECRET);
      String bodyStr = sendGet(url, new HashMap<>());
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

    String url = String.format(WxConstants.BASE_URL + WxConstants.JSCODE_TO_SESSION, APPID, APPSECRET,jsCode);
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("appid", APPID);
    paramMap.put("secret", APPSECRET);
    paramMap.put("js_code", jsCode);
    paramMap.put("grant_type", GRANT_TYPE);
    String bodyStr = sendGet(url, paramMap);
    return JSONUtil.toBean(bodyStr, WxJscodeToSessionResponse.class);

  }

  private static String sendGet(String url, Map<String, Object> paramMap){
    log.info("baseUrl：{}", url);

    String bodyStr = "";
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");
    headers.put("Connection", "keep-alive");
    try {
      bodyStr = OkHttpUtils.doGet(url, paramMap, headers);
      log.info("bodyStr：{}", bodyStr);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return bodyStr;
  }

}
