package com.erp.server.dmp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 谷仓OPENAPI封装
 *
 * @Author Cloud
 * @Date 2023/3/29 10:30
 **/
@Slf4j
@Component
public class GoodCangApiUtils {
    private static Integer APP_KEY;

    private static String APP_token;

    private static String URL;
    @Value("${openApi.goodcang.appKey}")
    public void setAppKey(Integer appKey){
        GoodCangApiUtils.APP_KEY = appKey;
    }
    @Value("${openApi.goodcang.appToken}")
    public void setSecretKey(String secretKey) {
        GoodCangApiUtils.APP_token = secretKey;
    }
    @Value("${openApi.goodcang.url}")
    public void setSessionKey(String url) {
        GoodCangApiUtils.URL = url;
    }




}
