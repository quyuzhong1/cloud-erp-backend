package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 退货订单数据传输对象
 * @author Cloud
 * @Classname ReturnOrderDataDTO
 */

@Data
@NoArgsConstructor
public class PlatformReturnOrderDataDTO implements Serializable {
    // Order related fields and methods

    /**
     * 平台名称
     */
    private String platformSign;
}