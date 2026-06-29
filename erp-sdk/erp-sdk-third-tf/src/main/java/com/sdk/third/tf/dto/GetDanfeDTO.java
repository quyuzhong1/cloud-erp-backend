package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 获取发票Danfe请求DTO
 * 对应接口路径：/api/invoice/get_danfe
 * Danfe URL链接就是PDF文件
 * 
 * @author system
 * @date 2025/01/XX
 */
@Data
public class GetDanfeDTO implements Serializable {

    /**
     * 发票UUID
     */
    @NotBlank(message = "发票UUID不能为空")
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 简版Danfe高度，单位mm
     */
    @JsonProperty("altura")
    private Integer altura;

    /**
     * 简版Danfe宽度，单位mm
     */
    @JsonProperty("largura")
    private Integer largura;
}
