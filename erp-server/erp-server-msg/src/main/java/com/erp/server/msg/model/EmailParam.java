package com.erp.server.msg.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @Classname: EmailContent
 * @Description: 邮件消息实体
 * @CreateTime: 2023-04-20  14:57
 * @Author: zhangchunlin
 */
@Data
public class EmailParam extends BaseNoticeMsgParam implements Serializable {

    /**
     * 接收者邮箱集合
     */
    private Set<String> receiverEmails;

    /**
     * 抄送者邮箱集合
     */
    private Set<String> cc;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容(可写入HTML)
     */
    private String content;

    /**
     * 邮件附件链接集合
     */
    private List<String> urls;

}