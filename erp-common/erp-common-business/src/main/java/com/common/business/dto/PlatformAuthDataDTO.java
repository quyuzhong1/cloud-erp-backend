package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 销售订单数据传输对象
 * @author Cloud
 * @Classname OrderDataDTO
 */

@Data
@NoArgsConstructor
public class PlatformAuthDataDTO implements Serializable {
    // Order related fields and methods

    /**
     * 平台名称
     */
    private String platformSign;
}