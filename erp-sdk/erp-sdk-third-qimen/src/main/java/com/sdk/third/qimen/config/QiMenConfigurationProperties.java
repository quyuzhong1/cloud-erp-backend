package com.sdk.third.qimen.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 奇门配置参数
 * @date 2024-06-06
 * @author tanmujin
 */
@Component
@Data
@ConfigurationProperties(prefix = "qimen")
public class QiMenConfigurationProperties {
    /**
     * 奇门服务地址
     */
    private String serverUrl = "";
    /**
     * 奇门appKey
     */
    private String appKey = "";
    /**
     * 奇门appSecret
     */
    private String appSecret = "";

    /**
     * 目标应用的AppKey：这里是通过奇门查询旺店通数据，则为旺店通的AppKey，固定值
     */
    private String targetAppKey = "21363512";

    /**
     * 旺店通appKey
     */
    private String wdtAppKey = "";

    /**
     * 旺店通appSecret<br/>
     * 其由两部分构成, 即wdtAppSecret = {wdtSecret}:{wdtSalt}
     */
    private String wdtAppSecret = "";

    /**
     * 旺店通secret<br/>
     * 对应旺店通appSecret的前半部分
     */
    private String wdtSecret = "";
    /**
     * 旺店通salt<br/>
     * 对应wdtAppSecret的后半部分
     */
    private String wdtSalt = "";

    /**
     * 旺店通自定义参数，该信息由旺店通分配
     */
    private String wdt3CustomerId = "wjkj03";
}
