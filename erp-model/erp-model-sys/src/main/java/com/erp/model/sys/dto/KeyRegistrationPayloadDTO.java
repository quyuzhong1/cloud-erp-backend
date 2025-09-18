package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 会话密钥协商Payload DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
public class KeyRegistrationPayloadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 前端生成的对称密钥
     */
    private String symmetricKey;
}
