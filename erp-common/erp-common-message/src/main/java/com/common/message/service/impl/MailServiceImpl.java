package com.common.message.service.impl;


import com.common.core.utils.FileUtil;
import com.common.core.utils.ThymeleafUtil;
import com.common.message.dto.email.EmailDTO;
import com.common.message.dto.email.EmailVerifyCodeDTO;
import com.common.message.service.MailService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.ObjectUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Classname MailServiceImpl
 * @Description TODO
 * @Date 2022-07-28 11:30
 * @Created by yl
 */
@Slf4j
@AllArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    private final MailProperties mailProperties;

    private final String nickname;

    /**
     * 发送文本邮件
     *
     * @param to      收件人地址地址
     * @param subject 　邮件主题
     * @param content 　邮件内容
     * @param cc      抄送地址
     */
    @Override
    public void sendSimpleMail(String to, String subject, String content, String... cc) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(nickname +'<' + mailProperties.getUsername() + '>');
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
     * 批量发送HTML邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc      抄送地址
     * @throws MessagingException 邮件发送异常
     */
    @Override
    public void batchSendHtmlMail(String[] to, String subject, String content, String... cc) throws MessagingException {
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
        helper.setFrom(nickname +'<' + mailProperties.getUsername() + '>');
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        if (!ObjectUtils.isEmpty(cc)) {
            helper.setCc(cc);
        }
        return helper;
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
    private MimeMessageHelper buildHelper(String[] to, String subject, String content, MimeMessage message, String[] cc) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(nickname +'<' + mailProperties.getUsername() + '>');
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        if (!ObjectUtils.isEmpty(cc)) {
            helper.setCc(cc);
        }
        return helper;
    }


    /**
     * 发送邮箱验证码
     *
     * @return void
     * @author yl
     * @date 2022-08-02 10:32
     * 发送邮箱验证码
     */

    @Override
    public Boolean sendVerifyCode(EmailDTO<EmailVerifyCodeDTO> dto) {
        Boolean flag = true;
        EmailVerifyCodeDTO emailDTO = dto.getData();
        //读取邮件模板
        String html = this.extractTemplate(dto.getTemplate());
        if (StringUtils.isBlank(html)) {
            throw new ServiceException(ApiError.ERROR_1006);
        }
        String content = ThymeleafUtil.generateTemplate(html, emailDTO);
        try {
            this.batchSendHtmlMail(dto.getRecipients(), dto.getSubject(), content, null);
        } catch (MessagingException e) {
            flag = false;
            throw new ServiceException(ApiError.ERROR_1010);
        }

        return flag;
    }


    /**
     * 读取配置文件
     *
     * @param templateName
     * @return java.lang.String
     * @author yl
     * @date 2022-08-02 11:04
     */
    private String extractTemplate(String templateName) {
        //读取邮件模板
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        InputStream is;
        String html = null;
        try {
            is = resourceLoader.getResource("classpath:email/" + templateName + ".html").getInputStream();
            html = FileUtil.readFile(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }
}
