package com.erp.tms.batong.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname BaseResult
 * @Description 基础返回结果
 * @Date 2024-01-15 9:19
 * @Created by yl
 */
@Data
public class BaseResult<T> implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 是否成功标志，0代表失败；1代表成功
      */
    private Integer success;

    /**
     * 中文消息
     */
    private String cnmessage;

    /**
     * enmessage
     */
    private String enmessage;

    private T data;

}
