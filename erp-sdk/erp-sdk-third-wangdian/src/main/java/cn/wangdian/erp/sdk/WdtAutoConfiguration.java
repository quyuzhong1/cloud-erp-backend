package cn.wangdian.erp.sdk;

import cn.wangdian.erp.sdk.impl.DefaultClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 旺店通客户端自动配置
 *
 * @author tanmujin
 * @date 2024-05-11
 */
@ConditionalOnClass({DefaultClient.class, WdtProperties.class})
@ConditionalOnMissingBean(Client.class)
@EnableConfigurationProperties(WdtProperties.class)
@Configuration
public class WdtAutoConfiguration {
    private final WdtProperties wdtProperties;

    public WdtAutoConfiguration(WdtProperties wdtProperties) {
        this.wdtProperties = wdtProperties;
    }

    @Bean
    public Client defaultClient() {
        return DefaultClient.get(wdtProperties.getSid(), wdtProperties.getUrl(), wdtProperties.getAppKey(), wdtProperties.getAppSecret());
    }
}
