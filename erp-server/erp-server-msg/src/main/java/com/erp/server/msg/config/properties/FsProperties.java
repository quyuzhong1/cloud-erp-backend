package com.erp.server.msg.config.properties;

import com.erp.server.msg.model.FeishuConfigParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * @Classname FsProperties
 * @Description 飞书配置
 * @Date 2022-07-20 17:57
 * @Created by yl
 */
@Data
@ConfigurationProperties(prefix = FsProperties.PREFIX)
@Component
@RefreshScope
public class FsProperties {

    public static final String PREFIX = "third.fs";

    private Map<String, FeishuConfigParam> configs;// 飞书发送任务通知配置

    private Map<String, String> warns;// 飞书发送预警通知配置

    private Boolean warnSend;// 飞书发送预警通知开关，为false则不发送

}
