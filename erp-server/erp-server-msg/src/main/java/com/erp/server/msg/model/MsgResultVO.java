package com.erp.server.msg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname: MsgResultVO
 * @Description: 消息响应实体
 * @CreateTime: 2023-04-23  09:31
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class MsgResultVO<T> implements Serializable {

    /**
     * 响应结果
     */
    private Integer code;

    /**
     * 响应描述
     */
    private String msg;

    /**
     * 响应内容
     */
    private T data;

    /**
     * 是否需要重新发送
     */
    private Boolean needReSend = Boolean.FALSE;

    /**
     * 请求参数
     */
    private String requestBody;

    /**
     * 是否成功
     *
     * @return
     */
    public boolean isSuccess() {
        return code.equals(200);
    }


}