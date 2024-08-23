package com.sdk.oms.tiktok.dto.tiktok.token;

import com.google.gson.annotations.SerializedName;
import com.sdk.oms.tiktok.dto.tiktok.shop.ShopsBean;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenDTO {
    /**
     * access_token : ROW_Fw8rBwAAAAAkW03FYd09DG-9INtpw361hWthei8S3fHX8iPJ5AUv99fLSCYD9-UucaqxTgNRzKZxi5-tfFMtdWqglEt5_iCk
     * access_token_expire_in : 1660556783
     * refresh_token : NTUxZTNhYTQ2ZDk2YmRmZWNmYWY2YWY2YzkxNGYwNjQ3YjkzYTllYjA0YmNlMw
     * refresh_token_expire_in : 1691487031
     * open_id : 7010736057180325637
     * seller_name : Jjj test shop
     * seller_base_region : ID
     * user_type : 0
     */

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("access_token_expire_in")
    private Integer accessTokenExpireIn;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("refresh_token_expire_in")
    private Integer refreshTokenExpireIn;
    @SerializedName("open_id")
    private String openId;
    @SerializedName("seller_name")
    private String sellerName;
    @SerializedName("seller_base_region")
    private String sellerBaseRegion;
    @SerializedName("user_type")
    private Integer userType;
    @SerializedName("cipher")
    private String shopCipher;
    @SerializedName("seller_type")
    private String sellerType;

    private ShopsBean shopsBean;

}
