package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 取消发票请求DTO（新接口）
 * 对应新接口路径：/api/invoice/cancel
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class CancelInvoiceDTO implements Serializable {

    /**
     * 要取消的发票的uuid
     */
    @NotBlank(message = "发票UUID不能为空")
    @JsonProperty("uuid")
    private String uuid;
}
