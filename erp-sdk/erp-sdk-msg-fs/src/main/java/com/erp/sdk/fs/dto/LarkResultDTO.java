package com.erp.sdk.fs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 飞书返回结果接收类
 *
 * @Author Cloud
 * @Date 2023/3/9 14:29
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LarkResultDTO<T> implements Serializable {
    private static final long serialVersionUID = 2405172041950251807L;

    private Integer code;

    private String msg;

    private String data;
}
