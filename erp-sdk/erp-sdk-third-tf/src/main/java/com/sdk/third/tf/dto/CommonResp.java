package com.sdk.third.tf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CommonResp {

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("msg")
    private String msg;
}
