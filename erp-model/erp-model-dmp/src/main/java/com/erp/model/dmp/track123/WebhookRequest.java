package com.erp.model.dmp.track123;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zdy
 * @ClassName WebhookRequest
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebhookRequest implements Serializable {
    private String platform;
    private Map<String, Object> payload;
}
