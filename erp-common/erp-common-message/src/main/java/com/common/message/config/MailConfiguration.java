package com.common.message.config;

import com.common.message.service.MailService;
import com.common.message.service.impl.MailServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
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
public class MailConfiguration  {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private MailProperties mailProperties;

    @Value("${spring.mail.nickname}")
    private String nickname;

    @Bean
    public MailService mailService() {
        if(null== nickname || nickname.length()<1){
            nickname = "唯迹ERP系统";
        }
        return new MailServiceImpl(mailSender,mailProperties, nickname);
    }
}
