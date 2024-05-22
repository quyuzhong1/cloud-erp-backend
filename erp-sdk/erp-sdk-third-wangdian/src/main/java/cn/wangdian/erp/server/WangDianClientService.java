package cn.wangdian.erp.server;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.WdtProperties;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
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

