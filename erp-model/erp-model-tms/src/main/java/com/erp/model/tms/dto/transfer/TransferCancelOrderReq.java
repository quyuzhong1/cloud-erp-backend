package com.erp.model.tms.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TransferCancelOrderReq {

    /**
     * 第三方平台单号
     */
    @NotBlank(message = "第三方平台单号不能为空")
    private String thirdPlatformCode;

    /**
     * 取消原因
     */
    @NotBlank(message = "取消原因不能为空")
    private String reason;
}
