package com.sdk.oms.shopify.api.dto;

import com.google.gson.annotations.SerializedName;

public class AssociatedUserBean {
    /**
     * id : 902541635
     * first_name : John
     * last_name : Smith
     * email : john@example.com
     * email_verified : true
     * account_owner : true
     * locale : en
     * collaborator : false
     */

    @SerializedName("id")
    private int id;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isAccountOwner() {
        return accountOwner;
    }

    public void setAccountOwner(boolean accountOwner) {
        this.accountOwner = accountOwner;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isCollaborator() {
        return collaborator;
    }

    public void setCollaborator(boolean collaborator) {
        this.collaborator = collaborator;
    }
}
