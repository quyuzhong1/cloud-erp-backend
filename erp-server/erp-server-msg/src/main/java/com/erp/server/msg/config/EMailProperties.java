package com.erp.server.msg.config;

import com.erp.server.msg.model.MailConfigParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Classname: MailProperties
 * @Description: TODO
 * @CreateTime: 2023-04-23  09:49
 * @Author: zhangchunlin
 */
@Data
@ConfigurationProperties(prefix = EMailProperties.PREFIX)
@Component
@RefreshScope
public class EMailProperties {

    public static final String PREFIX = "third.mails";

    private Map<String, MailConfigParam> configs;

}