package com.common.message.service;

import javax.mail.MessagingException;

/**
 * @Classname 邮件服务
 * @Description TODO
 * @Date 2022-07-28 11:28
 * @Created by yl
 */
public interface MailService{

    /**
     * 发送文本邮件
     *
     * @param to      　　　　　收件人地址
     * @param subject 　　邮件主题
     * @param content 　　邮件内容
     * @param cc      　　　　　抄送地址
     */
    void sendSimpleMail(String to, String subject, String content, String... cc);


    /**
     * 发送HTML邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc      抄送地址
     * @throws MessagingException 邮件发送异常
     */
    void sendHtmlMail(String to, String subject, String content, String... cc) throws MessagingException;


    void batchSendHtmlMail(String[] to, String subject, String content, String... cc) throws MessagingException;


    /**
     * 发送带附件的邮件
     *
     * @param to       收件人地址
     * @param subject  邮件主题
     * @param content  邮件内容
     * @param filePath 附件地址
     * @param cc       抄送地址
     * @throws MessagingException 邮件发送异常
     */
    void sendAttachmentMail(String to, String subject, String content, String filePath, String... cc) throws MessagingException;


    void sedVerifyCode(com.erp.common.dto.email.EmailDTO<com.erp.common.dto.email.EmailVerifyCodeDTO> dto);
}
