package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

public class BuyerBeanX {
    /**
     * id : 139133205
     * nickname : WILBERTALONZO
     * last_name : ALONZO
     * first_name : WILBERT
     */

    @SerializedName("id")
    private int id;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("first_name")
    private String firstName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
