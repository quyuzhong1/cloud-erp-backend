package com.sdk.oms.mercado.dto.mercado;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlatformMercadoTokenDTO {

    /**
     * access_token : APP_USR-5387223166827464-090515-8cc4448aac10d5105474e135355a8321-8035443
     * token_type : bearer
     * expires_in : 10800
     * scope : offline_access read write
     * user_id : 8035443
     * refresh_token : TG-5b9032b4e4b0714aed1f959f-8035443
     */

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private int expiresIn;
    @SerializedName("scope")
    private String scope;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("refresh_token")
    private String refreshToken;
}
