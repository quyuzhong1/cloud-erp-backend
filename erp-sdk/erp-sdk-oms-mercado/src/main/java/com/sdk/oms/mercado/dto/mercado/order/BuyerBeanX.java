package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class BuyerBeanX {
    /**
     * id : 139133205
     * nickname : WILBERTALONZO
     * last_name : ALONZO
     * first_name : WILBERT
     */

    @SerializedName("id")
    private int fid;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("first_name")
    private String firstName;

}
