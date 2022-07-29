package com.common.message.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;

/**
 * @Classname 邮件配置
 * @Description TODO
 * @Date 2022-07-28 11:15
 * @Created by yl
 */
@Configuration
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
public class MailConfiguration {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private MailProperties mailProperties;
}
