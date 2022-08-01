package com.common.message.service.impl;

import com.common.message.service.MailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.ObjectUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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


    /**
     * 发送HTML邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc      抄送地址
     * @throws MessagingException 邮件发送异常
     */
    @Override
    public void sendHtmlMail(String to, String subject, String content, String... cc) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = buildHelper(to, subject, content, message, cc);
        mailSender.send(message);
    }




    /**
     * 统一封装MimeMessageHelper
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param message 消息对象
     * @param cc      抄送地址
     * @return MimeMessageHelper
     * @throws MessagingException 异常
     */
    private MimeMessageHelper buildHelper(String to, String subject, String content, MimeMessage message, String[] cc) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailProperties.getUsername());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        if (!ObjectUtils.isEmpty(cc)) {
            helper.setCc(cc);
        }
        return helper;
    }

    @Override
    public void sendAttachmentMail(String to, String subject, String content, String filePath, String... cc) throws MessagingException {

    }
}
