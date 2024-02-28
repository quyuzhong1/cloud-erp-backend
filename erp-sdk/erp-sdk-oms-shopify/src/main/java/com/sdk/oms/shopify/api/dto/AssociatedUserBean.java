package com.sdk.oms.shopify.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AssociatedUserBean {

    /**
     * 平台卖家id/用户id
     */
    @SerializedName("id")
    private int id;
    /**
     * 用户名
     */
    @SerializedName("first_name")
    /**
     * 用户名
     */
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    /**
     * 用户邮箱
     */
    @SerializedName("email")
    private String email;
    @SerializedName("email_verified")
    private boolean emailVerified;
    @SerializedName("account_owner")
    private boolean accountOwner;
    @SerializedName("locale")
    private String locale;
    @SerializedName("collaborator")
    private boolean collaborator;

}
