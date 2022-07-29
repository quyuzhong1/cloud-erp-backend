package com.common.message.service.impl;

import com.common.message.service.MailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.ObjectUtils;

/**
 * @Classname MailServiceImpl
 * @Description TODO
 * @Date 2022-07-28 11:30
 * @Created by yl
 */
@Slf4j
@AllArgsConstructor
public class MailServiceImpl  implements MailService {

    private final JavaMailSender mailSender;

    private final MailProperties mailProperties;




    /**
     * 发送文本邮件
     *
     * @param to    收件人地址
     * @param subject 　邮件主题
     * @param content 　邮件内容
     * @param cc    抄送地址
     */
    @Override
    public void sendSimpleMail(String to, String subject, String content, String... cc) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getUsername());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        if (!ObjectUtils.isEmpty(cc)) {
            message.setCc(cc);
        }
        mailSender.send(message);
    }
}
