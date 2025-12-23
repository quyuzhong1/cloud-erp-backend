package com.sdk.oms.pdd.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class AuthTokenCreateResponse extends PopBaseHttpResponse{

    @JSONField(name ="pop_auth_token_create_response")
    private PopAuthTokenCreateResponse popAuthTokenCreateResponse;

    public AuthTokenCreateResponse() {
    }

    @Data
    public static class PopAuthTokenCreateResponse {
        @JSONField(name ="access_token")
        private String accessToken;
        @JSONField(name ="expires_at")
        private Long expiresAt;
        @JSONField(name ="expires_in")
        private Integer expiresIn;
        @JSONField(name ="owner_id")
        private String ownerId;
        @JSONField(name ="owner_name")
        private String ownerName;
        @JSONField(name ="r1_expires_at")
        private Long r1ExpiresAt;
        @JSONField(name ="r1_expires_in")
        private Integer r1ExpiresIn;
        @JSONField(name ="r2_expires_at")
        private Long r2ExpiresAt;
        @JSONField(name ="r2_expires_in")
        private Integer r2ExpiresIn;
        @JSONField(name ="refresh_token")
        private String refreshToken;
        @JSONField(name ="refresh_token_expires_at")
        private Long refreshTokenExpiresAt;
        @JSONField(name ="refresh_token_expires_in")
        private Integer refreshTokenExpiresIn;
        @JSONField(name ="scope")
        private List<String> scope;
        @JSONField(name ="w1_expires_at")
        private Long w1ExpiresAt;
        @JSONField(name ="w1_expires_in")
        private Integer w1ExpiresIn;
        @JSONField(name ="w2_expires_at")
        private Long w2ExpiresAt;
        @JSONField(name ="w2_expires_in")
        private Integer w2ExpiresIn;

        public PopAuthTokenCreateResponse() {
        }

    }
}
