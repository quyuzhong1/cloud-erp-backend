package com.sdk.wangdian.server;

import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.WdtProperties;
import com.sdk.wangdian.sdk.impl.ApiFactory;
import com.sdk.wangdian.sdk.impl.DefaultClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 旺店通连接工具类
 * @date 2024-05-21
 * @author tanmujin
 */
@Component
@EnableConfigurationProperties(WdtProperties.class)
public class WangDianClientService {

    private Client defaultClient;

    WangDianClientService(WdtProperties wdtProperties){
        this.defaultClient = DefaultClient.get(wdtProperties.getSid(), wdtProperties.getUrl(), wdtProperties.getAppKey(), wdtProperties.getAppSecret());
    }

    public <T> T get(Class<T> clazz){
        return ApiFactory.get(this.defaultClient, clazz);
    }
}

