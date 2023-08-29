package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 退款数据传输对象
 * @author Cloud
 */

@Data
@NoArgsConstructor
public class PlatformRefundDataDTO implements Serializable {
    // Refund related fields and methods

    /**
     * 平台名称
     */
    private String platformSign;
}