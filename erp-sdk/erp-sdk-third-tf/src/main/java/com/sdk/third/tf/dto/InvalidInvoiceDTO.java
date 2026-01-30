package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 作废发票请求DTO（新接口）
 * 对应新接口路径：/api/invoice/invalid
 * 作废发票一般使用场景为发票号跳号时使用
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class InvalidInvoiceDTO implements Serializable {

    /**
     * 发票模板，默认为55
     */
    @NotBlank(message = "发票模板不能为空")
    @JsonProperty("modelo")
    private String modelo;

    /**
     * 1为生产环境，2为测试环境，不具法律效力
     */
    @NotBlank(message = "运行环境不能为空")
    @JsonProperty("ambiente")
    private String ambiente;

    /**
     * 发票序列号
     */
    @NotBlank(message = "发票序列号不能为空")
    @JsonProperty("serie")
    private String serie;

    /**
     * 发票编号：作废起始号
     */
    @NotBlank(message = "作废起始号不能为空")
    @JsonProperty("start_number")
    private String startNumber;

    /**
     * 发票编号：作废结束号
     */
    @NotBlank(message = "作废结束号不能为空")
    @JsonProperty("end_number")
    private String endNumber;

    /**
     * 作废描述
     */
    @NotBlank(message = "作废原因不能为空")
    @JsonProperty("motivo")
    private String motivo;
}
