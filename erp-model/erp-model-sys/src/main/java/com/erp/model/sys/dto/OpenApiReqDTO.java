package com.erp.model.sys.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OpenApiReqDTO {

    @NotNull(message = "请传入请求时间戳(毫秒)")
    private Long timestamp;// 当前请求时间戳，毫秒

    @NotBlank(message = "请传入签名类型")
    private String signType;// 签名类型 AES

    @NotBlank(message = "请传入签名串")
    private String sign;// 签名串

    @NotBlank(message = "请传入接口版本号")
    private String version;// 接口版本号 1.0

    @NotBlank(message = "请传入接口方法名")
    private String serviceMethod;// 接口名

    @NotBlank(message = "请传入编码格式")
    private String charset;

    // 业务参数
    private String bizContent;

}
