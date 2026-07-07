package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 快递100订阅回调响应
 *
 * @author jack
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100WebhookResponseDTO implements Serializable {

    private Boolean result;

    private String returnCode;

    private String message;

    public static Kuaidi100WebhookResponseDTO success() {
        return Kuaidi100WebhookResponseDTO.builder()
                .result(Boolean.TRUE)
                .returnCode("200")
                .message("成功")
                .build();
    }

    public static Kuaidi100WebhookResponseDTO failure(String returnCode, String message) {
        return Kuaidi100WebhookResponseDTO.builder()
                .result(Boolean.FALSE)
                .returnCode(returnCode)
                .message(message)
                .build();
    }
}
