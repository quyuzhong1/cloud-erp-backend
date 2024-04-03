package com.sdk.oms.tictok.dto.tiktok.token;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlatformTikTokTokenDTO {

    /**
     * code : 0
     * message : success
     * data : {"access_token":"ROW_Fw8rBwAAAAAkW03FYd09DG-9INtpw361hWthei8S3fHX8iPJ5AUv99fLSCYD9-UucaqxTgNRzKZxi5-tfFMtdWqglEt5_iCk","access_token_expire_in":1660556783,"refresh_token":"NTUxZTNhYTQ2ZDk2YmRmZWNmYWY2YWY2YzkxNGYwNjQ3YjkzYTllYjA0YmNlMw","refresh_token_expire_in":1691487031,"open_id":"7010736057180325637","seller_name":"Jjj test shop","seller_base_region":"ID","user_type":0}
     * request_id : 2022080809462301024509910319695C45
     */

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private TokenDTO data;
    @SerializedName("request_id")
    private String requestId;
}
