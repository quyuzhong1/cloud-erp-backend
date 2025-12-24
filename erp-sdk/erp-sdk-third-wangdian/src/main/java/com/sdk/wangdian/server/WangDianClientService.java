package com.sdk.wangdian.server;

import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.WdtProperties;
import com.sdk.wangdian.sdk.impl.ApiFactory;
import com.sdk.wangdian.sdk.impl.DefaultClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 旺店通连接工具类
 * @date 2024-05-21
 * @author tanmujin
 */
@Slf4j
@EnableConfigurationProperties(WdtProperties.class)
public class WangDianClientService {

    private Client defaultClient;

    /*WangDianClientService(){
        this.defaultClient = DefaultClient.get("wdtapi3", "http://47.92.239.46/", "wjkj03-test", "b6412a9b6:806828718719806966febbfe948893e8");
    }*/

    WangDianClientService(WdtProperties properties){
//        this.defaultClient = DefaultClient.get("wjkj03", "https://wdt.wangdian.cn/", "wjkj03-ot", "af9a8ab635bdf3756a302f404a154a89:8479510573065989536f2cf426f8079a");
        this.defaultClient = DefaultClient.get(properties.getSid(), properties.getUrl(), properties.getAppKey(), properties.getAppSecret());
        log.info("初始化 WangDianClientService, {}", properties);
    }

    public <T> T get(Class<T> clazz){
        return ApiFactory.get(this.defaultClient, clazz);
    }
    
    public Client getClient() {
    	return defaultClient;
    }
}

