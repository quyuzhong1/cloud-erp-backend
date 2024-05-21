package cn.wangdian.erp.sdk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 旺店通配置绑定
 * @date 2024-05-09
 * @author tanmujin
 */
@Data
@ConfigurationProperties(prefix = "wdt")
public class WdtProperties {
    /**
     * 旺店通接口地址
     */
    private String url;

    /**
     * 卖家账号
     */
    private String sid;

    /**
     * 接口账号,在发送的数据中对应 key 字段
     */
    private String appKey;

    /**
     * 接口密钥
     */
    private String appSecret;
}
