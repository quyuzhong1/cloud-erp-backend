package com.erp.server.msg.utils;

import cn.hutool.extra.mail.MailAccount;
import com.erp.server.msg.model.MailConfigParam;

import java.util.Objects;

/**
 * @Classname: MailSendUtil
 * @Description: 邮件发送辅助工具类
 * @CreateTime: 2023-04-23  10:51
 * @Author: zhangchunlin
 */
public class MailSendUtil {

    /**
     * 填充邮件发送帐号信息
     * @param mailConfigParam
     * @return
     */
    public static MailAccount wrapSendMailAccount(MailConfigParam mailConfigParam) {
        MailAccount account = new MailAccount();
        account.setHost(mailConfigParam.getHost());
        account.setAuth(true);
        //不要设置user属性
        account.setFrom(mailConfigParam.getUsername());
        account.setPass(mailConfigParam.getPassword());
        if(Objects.equals(mailConfigParam.getUseSSl(),Boolean.TRUE)) {
            // 使用SSL安全连接
            account.setSslEnable(true);
            account.setStarttlsEnable(true);
            // 指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字
            account.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
            // 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
            account.setSocketFactoryFallback(true);
            // 指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口456
            account.setSocketFactoryPort(Objects.nonNull(mailConfigParam.getSslPort()) ? mailConfigParam.getSslPort() : 465);
        } else {
            account.setPort(25);
        }
        return account;
    }

}