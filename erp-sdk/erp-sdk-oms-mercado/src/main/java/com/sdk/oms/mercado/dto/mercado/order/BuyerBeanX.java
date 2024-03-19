package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BuyerBeanX {
    /**
     * id : 139133205
     * nickname : WILBERTALONZO
     * last_name : ALONZO
     * first_name : WILBERT
     */

    @JsonProperty("id")
    private int fid;
    @JsonProperty("nickname")
    private String nickname;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("first_name")
    private String firstName;

}
