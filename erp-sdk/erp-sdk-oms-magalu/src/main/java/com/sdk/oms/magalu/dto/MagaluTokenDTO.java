package com.sdk.oms.magalu.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MagaluTokenDTO {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "refresh_token")
    private String refreshToken;

    @JSONField(name = "token_type")
    private String tokenType;

    @JSONField(name = "expires_in")
    private Integer expiresIn;

    @JSONField(name = "scope")
    private String scope;

    @JSONField(name = "created_at")
    private Long createdAt;
}
