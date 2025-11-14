package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 单点登录Payload DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
public class SsoPayloadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用类型：飞书、PDA、微信小程序、ERP
     */
    private String appType;

    /**
     * 飞书用户唯一标识
     */
    private String unionId;

    /**
     * 前端生成的对称密钥
     */
    private String symmetricKey;
}
