package com.erp.common.modules.email.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 发送邮件通用数据传输类
 */
@Data
@NoArgsConstructor
public class EmailDTO<T> implements Serializable {


    /**
     * 主题
     */

    String subject;

    /**
     * 邮件模版
     */

    String template;

    /**
     * 收件人列表
     */

    String[] recipients;

    /**
     * 邮件内容参数
     */
    @NotNull(message = "参数不能为空")
    protected T data;


    /**
     * 获取收件人列表，用逗号隔开的
     *
     * @return
     */
    public String getRecipientsString() {
        return connectAddress(this.recipients);
    }

    /**
     * 连接地址
     *
     * @param emails 邮箱
     * @return
     */
    String connectAddress(String[] emails) {
        StringBuffer sb = null;
        for (String email : emails) {
            if (sb != null) {
                sb.append(",").append(email);
            } else {
                sb = new StringBuffer(email);
            }
        }
        return sb.toString();
    }
}