package com.sdk.oms.temu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemuCommonDTO {

    private String areaCode;

    private String appKey;

    private String appSecret;

    private String token;

}
