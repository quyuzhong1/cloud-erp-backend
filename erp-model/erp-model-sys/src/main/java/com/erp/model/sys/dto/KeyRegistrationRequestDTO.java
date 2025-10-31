package com.erp.model.sys.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 会话密钥协商请求DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
public class KeyRegistrationRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 使用RSA公钥加密的payload
     * 包含：symmetricKey（前端生成的对称密钥）
     */
    @NotBlank(message = "加密payload不能为空")
    private String encryptedPayload;
}
