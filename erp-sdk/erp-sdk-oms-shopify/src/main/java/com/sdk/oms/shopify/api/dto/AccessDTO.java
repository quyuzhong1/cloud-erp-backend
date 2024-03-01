package com.sdk.oms.shopify.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AccessDTO {

    /**
     * access_token : f85632530bf277ec9ac6f649fc327f17
     * scope : write_orders,read_customers
     * expires_in : 86399
     * associated_user_scope : write_orders
     * associated_user : {"id":902541635,"first_name":"John","last_name":"Smith","email":"john@example.com","email_verified":true,"account_owner":true,"locale":"en","collaborator":false}
     */

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("scope")
    private String scope;
    @SerializedName("expires_in")
    private int expiresIn;
    @SerializedName("associated_user_scope")
    private String associatedUserScope;
    @SerializedName("associated_user")
    private AssociatedUserBean associatedUser;


    public AccessDTO() {
        this.accessToken = "";
        this.scope = "";
        this.expiresIn = 0;
        this.associatedUserScope = "";
    }
}
