package com.erp.server.msg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname: LarkResultVO
 * @Description: 飞书响应实体
 * @CreateTime: 2023-04-19  19:52
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class LarkResultVO<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

}